package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.enums.NotificationType;
import com.abonex.abonexbackend.repository.SubscriptionRepository;
import com.abonex.abonexbackend.service.fcm.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduleService {
    private final SubscriptionRepository subscriptionRepository;
    private final FCMService fcmService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkSubscriptionsAndNotify() {
        List<Subscription> subscriptions = subscriptionRepository.findAllByIsActiveTrueAndNextPaymentDateIsNotNull();
        LocalDate today = LocalDate.now();

        for (Subscription sub : subscriptions) {
            User user = sub.getUser();
            if (user.getFcmToken() == null || user.getFcmToken().isBlank()) continue;

            long daysUntilPayment = ChronoUnit.DAYS.between(today, sub.getNextPaymentDate());

            if (daysUntilPayment >= 0 && daysUntilPayment <= sub.getNotificationDaysBefore()) {
                sendNotificationFor(user, sub, daysUntilPayment);
            }
        }
    }

    private void sendNotificationFor(User user, Subscription sub, long daysUntil) {
        String title = "Abonelik Hatırlatıcısı";
        String body = String.format(
                "%s aboneliğinizin ödemesine son %d gün!",
                sub.getSubscriptionName(),
                daysUntil
        );

        Map<String, String> data = Map.of(
                "notificationType", NotificationType.UPCOMING_PAYMENT.name(),
                "subscriptionId", sub.getId().toString()
        );

        fcmService.sendNotificationWithData(user.getFcmToken(), title, body, data);
    }
}