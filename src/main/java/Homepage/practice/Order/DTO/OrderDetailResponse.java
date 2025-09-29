package Homepage.practice.Order.DTO;

import Homepage.practice.Delivery.DTO.DeliveryResponse;
import Homepage.practice.Order.Order;
import Homepage.practice.OrderItem.DTO.OrderItemResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {
    private Long id;
    private LocalDate orderDate;        // 주문 날짜
    private BigDecimal totalPrice;      // 주문 총 가격
    private DeliveryResponse deliveryResponse;
    private List<OrderItemResponse> orderItemResponses;

    public static OrderDetailResponse fromEntity(Order order) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalPrice(order.getTotalPrice())
                .deliveryResponse(DeliveryResponse.fromEntity(order.getDelivery()))
                .orderItemResponses(order.getOrderItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
