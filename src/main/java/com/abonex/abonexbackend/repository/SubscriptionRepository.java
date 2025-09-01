package com.abonex.abonexbackend.repository;

import com.abonex.abonexbackend.entity.Subscription;
import com.abonex.abonexbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    List<Subscription> findAllByIsActiveTrueAndNextPaymentDate(LocalDate date);
}
