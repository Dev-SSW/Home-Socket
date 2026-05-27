package Homepage.practice.Payment;

import Homepage.practice.Order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime approvedAt;
    private LocalDateTime failedAt;
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 생성 */
    public static Payment create(Order order, BigDecimal amount) {
        return Payment.builder()
                .order(order)
                .amount(amount)
                .status(PaymentStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** 비즈니스 로직 */
    // 승인됨 처리
    public void approve(String paymentKey) {
        if (this.status == PaymentStatus.APPROVED) {
            return;
        }

        this.paymentKey = paymentKey;
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 실패함 처리
    public void fail(String reason) {
        if (this.status == PaymentStatus.APPROVED) {
            return;
        }

        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
