package com.abonex.abonexbackend.repository;

import com.abonex.abonexbackend.entity.PaymentHistory;
import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findBySubscriptionId(Long subscriptionId);
    List<PaymentHistory> findBySubscriptionUserAndPaymentDateBetween(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<PaymentHistory> findBySubscriptionOrderByPaymentDateDesc(Subscription subscription);
    List<PaymentHistory> findBySubscriptionUser(User user);

}
