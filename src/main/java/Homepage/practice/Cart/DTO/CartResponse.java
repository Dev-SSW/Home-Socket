package Homepage.practice.Cart.DTO;

import Homepage.practice.Cart.Cart;
import Homepage.practice.CartItem.DTO.CartItemResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {
    private Long id;
    private List<CartItemResponse> cartItemList;
    private int totalPrice;

    public static CartResponse fromEntity(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .cartItemList(cart.getCartItems().stream()
                        .map(CartItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .totalPrice(cart.getTotalPrice())
                .build();
    }
}
