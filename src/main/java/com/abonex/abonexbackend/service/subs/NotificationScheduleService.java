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
        log.info("Zamanlayıcı çalıştı, abonelikler kontrol ediliyor...");
        List<Subscription> subscriptions = subscriptionRepository.findAllByIsActiveTrue();
        LocalDate today = LocalDate.now();
        LocalDate upcomingLimit = today.plusDays(8);

        for (Subscription sub : subscriptions) {
            User user = sub.getUser();
            if (user.getFcmToken() == null || user.getFcmToken().isBlank()) continue;

            LocalDate nextPayment = sub.getNextPaymentDate();
            LocalDate endDate = sub.getEndDate();

            if (endDate != null && endDate.isBefore(today)) {
                sendNotificationFor(user, sub, NotificationType.SUBSCRIPTION_EXPIRED, 0);
            }
            else if (nextPayment != null && nextPayment.isBefore(today)) {
                long daysOverdue = ChronoUnit.DAYS.between(nextPayment, today);
                sendNotificationFor(user, sub, NotificationType.PAYMENT_OVERDUE, daysOverdue);
            }
            else if (nextPayment != null && nextPayment.isAfter(today) && nextPayment.isBefore(upcomingLimit)) {
                long daysUntil = ChronoUnit.DAYS.between(today, nextPayment);
                if (daysUntil == sub.getNotificationDaysBefore()) {
                    sendNotificationFor(user, sub, NotificationType.UPCOMING_PAYMENT, daysUntil);
                }
            }
        }
    }

    private void sendNotificationFor(User user, Subscription sub, NotificationType type, long days) {
        String title = "";
        String body = "";

        switch (type) {
            case UPCOMING_PAYMENT:
                title = "Yaklaşan Ödeme";
                body = String.format("'%s' aboneliğinizin ödemesine son %d gün!", sub.getSubscriptionName(), days);
                break;
            case PAYMENT_OVERDUE:
                title = "Gecikmiş Ödeme";
                body = String.format("'%s' aboneliğinizin ödemesi %d gün gecikti.", sub.getSubscriptionName(), days);
                break;
            case SUBSCRIPTION_EXPIRED:
                title = "Abonelik Sona Erdi";
                body = String.format("'%s' aboneliğinizin süresi doldu. Yenilemek veya kaldırmak için dokunun.", sub.getSubscriptionName());
                break;
        }

        Map<String, String> data = Map.of(
                "notificationType", type.name(),
                "subscriptionId", sub.getId().toString()
        );
        fcmService.sendNotificationWithData(user.getFcmToken(), title, body, data);
    }
}