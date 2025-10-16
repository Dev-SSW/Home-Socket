package Homepage.practice.Order.DTO;

import Homepage.practice.CartItem.DTO.CartItemResponse;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import Homepage.practice.CouponPublish.DTO.CouponPublishResponse;
import Homepage.practice.Delivery.DTO.AddressResponse;
import Homepage.practice.User.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderPageResponse {
    private List<AddressResponse> addressResponses;
    private List<CouponPublishResponse> couponPublishResponses;
    private List<CartItemResponse> cartItemResponses;

    public static OrderPageResponse of(User user) {
        return OrderPageResponse.builder()
                .addressResponses(user.getAddresses().stream()
                        .map(AddressResponse::fromEntity)
                        .collect(Collectors.toList()))
                .couponPublishResponses(user.getCouponPublishes().stream()
                        .filter(c -> c.getStatus() == CouponPublishStatus.AVAILABLE)
                        .map(CouponPublishResponse::fromEntity)
                        .collect(Collectors.toList()))
                .cartItemResponses(user.getCart().getCartItems().stream()
                        .map(CartItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
