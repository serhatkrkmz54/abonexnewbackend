package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduleService {
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 0 1 * * ?")
    public void checkUpcomingPaymentsAndNotify() {
        log.info("Yaklaşan ödemeler için bildirim kontrolü başladı...");

        List<Subscription> allActiveSubscriptions = subscriptionRepository.findAll();

        for (Subscription sub : allActiveSubscriptions) {
            if (!sub.isActive()) continue;

            long daysUntilPayment = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), sub.getNextPaymentDate());

            if (daysUntilPayment > 0 && daysUntilPayment == sub.getNotificationDaysBefore()) {
                log.info("BİLDİRİM: {} kullanıcısının {} aboneliğinin ödemesine {} gün kaldı.",
                        sub.getUser().getEmail(),
                        sub.getSubscriptionName(),
                        daysUntilPayment);

                // TODO: Mobil uygulamaya push notification gönderecek kod (FCM vb.) buraya entegre edilecek.
            }
        }
        log.info("Bildirim kontrolü tamamlandı.");
    }
}