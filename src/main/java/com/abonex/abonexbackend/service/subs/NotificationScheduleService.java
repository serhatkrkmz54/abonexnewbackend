package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.enums.NotificationType;
import com.abonex.abonexbackend.repository.SubscriptionRepository;
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
    private final NotificationCreationService notificationCreationService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkSubscriptionsAndNotify() {
        log.info("Zamanlayıcı çalıştı, abonelikler kontrol ediliyor...");
        List<Subscription> subscriptions = subscriptionRepository.findAllByIsActiveTrue();
        LocalDate today = LocalDate.now();

        for (Subscription sub : subscriptions) {
            LocalDate nextPayment = sub.getNextPaymentDate();
            LocalDate endDate = sub.getEndDate();

            if (endDate != null && endDate.isEqual(today)) {
                sendCategorizedNotification(sub.getUser(), sub, NotificationType.SUBSCRIPTION_EXPIRED, 0);
            }
            else if (nextPayment != null && nextPayment.isBefore(today)) {
                long daysOverdue = ChronoUnit.DAYS.between(nextPayment, today);
                if (daysOverdue == 1 || daysOverdue == 3 || daysOverdue == 7) {
                    sendCategorizedNotification(sub.getUser(), sub, NotificationType.PAYMENT_OVERDUE, daysOverdue);
                }
            }
            else if (nextPayment != null) {
                long daysUntil = ChronoUnit.DAYS.between(today, nextPayment);
                if (daysUntil > 0 && daysUntil == sub.getNotificationDaysBefore()) {
                    sendCategorizedNotification(sub.getUser(), sub, NotificationType.UPCOMING_PAYMENT, daysUntil);
                }
            }
        }
    }

    private void sendCategorizedNotification(User user, Subscription sub, NotificationType type, long days) {
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
                body = String.format("'%s' aboneliğinizin süresi bugün doluyor.", sub.getSubscriptionName());
                break;
        }

        notificationCreationService.createAndSendNotification(
                user,
                title,
                body,
                type,
                sub.getId()
        );
    }
}