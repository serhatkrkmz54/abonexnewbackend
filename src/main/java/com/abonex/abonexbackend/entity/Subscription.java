package com.abonex.abonexbackend.entity;

import com.abonex.abonexbackend.entity.enums.BillingCycle;
import com.abonex.abonexbackend.entity.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "subscription_name")
    private String subscriptionName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "next_payment_date", nullable = false)
    private LocalDate nextPaymentDate;

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "card_last_four_digits")
    private String cardLastFourDigits;

    @Column(name = "notification_days_before")
    private int notificationDaysBefore = 5;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentHistory> paymentHistories;
}
