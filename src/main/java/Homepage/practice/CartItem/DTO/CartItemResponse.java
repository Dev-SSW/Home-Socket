package Homepage.practice.CartItem.DTO;

import Homepage.practice.CartItem.CartItem;
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
public class CartItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;    // 아이템 이름 가져오기
    private int quantity;       // 수량
    private int price;          // 아이템 가격 가져오기
    private int totalPrice;     // 총 가격 (아이템 가격 * 수량)

    public static CartItemResponse fromEntity(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .itemId(cartItem.getItem().getId())
                .itemName(cartItem.getItem().getName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getItem().getItemPrice())
                .totalPrice(cartItem.getTotalPrice())
                .build();
    }
}
