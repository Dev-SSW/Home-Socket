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
    private String eventId;
    private Long orderId;
    private Long userId;
    private String username;
    private Long paymentId;
    private String paymentKey;
    private BigDecimal paidAmount;
    private LocalDateTime paidAt;
}
