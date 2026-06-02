package Homepage.practice.Event.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {
    String eventId;
    Long orderId;
    Long userId;
    Long paymentId;
    String paymentKey;
    BigDecimal paidAmount;
    LocalDateTime paidAt;
}
