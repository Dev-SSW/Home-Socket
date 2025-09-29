package Homepage.practice.OrderItem.DTO;

import Homepage.practice.OrderItem.OrderItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;    // 아이템 이름 가져오기
    private int quantity;       // 수량
    private int price;          // 아이템 가격 가져오기
    private int totalPrice;     // 총 가격 (아이템 가격 * 수량)

    public static OrderItemResponse fromEntity(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItem().getId())
                .itemName(orderItem.getItem().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getItem().getItemPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}
