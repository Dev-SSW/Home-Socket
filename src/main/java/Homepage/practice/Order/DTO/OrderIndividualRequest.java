package Homepage.practice.Order.DTO;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderIndividualRequest {
    @NotNull(message = "주소 ID를 입력하셔야 합니다.")
    private Long addressId;
    @NotNull(message = "발급 쿠폰 ID를 입력하셔야 합니다.")
    private Long couponPublishId;
    @NotNull(message = "상품 ID를 입력하셔야 합니다.")
    private Long itemId;
    @NotNull(message = "상품 수량을 입력하셔야 합니다.")
    private int quantity;
}
