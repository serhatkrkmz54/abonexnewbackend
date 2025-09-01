package com.abonex.abonexbackend.repository;

import com.abonex.abonexbackend.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findBySubscriptionId(Long subscriptionId);
}
