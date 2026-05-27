package Homepage.practice.Payment.DTO;

import Homepage.practice.Order.OrderStatus;
import Homepage.practice.Payment.Payment;
import Homepage.practice.Payment.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Long orderId;
    private Long paymentId;
    private String paymentKey;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .orderId(payment.getOrder().getId())
                .paymentId(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .orderStatus(payment.getOrder().getStatus())
                .build();
    }
}
