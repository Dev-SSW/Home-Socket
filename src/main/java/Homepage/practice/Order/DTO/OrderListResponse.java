package Homepage.practice.Order.DTO;

import Homepage.practice.Delivery.DeliveryStatus;
import Homepage.practice.Order.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderListResponse {
    private Long id;
    private LocalDate orderDate;        // 주문 날짜
    private BigDecimal totalPrice;      // 주문 총 가격
    private DeliveryStatus deliveryStatus; // 배송 상태
    
    public static OrderListResponse fromEntity(Order order) {
        return OrderListResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalPrice(order.getTotalPrice())
                .deliveryStatus(order.getDelivery().getStatus())
                .build();
    }
}
