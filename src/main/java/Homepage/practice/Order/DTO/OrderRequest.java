package Homepage.practice.Order.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {
    @NotNull(message = "주소 ID를 입력하셔야 합니다.")
    private Long addressId;
    @NotNull(message = "발급 쿠폰 ID를 입력하셔야 합니다.")
    private Long couponPublishId;
    @NotEmpty(message = "주문할 장바구니 아이템을 선택하셔야 합니다.")
    private List<Long> cartItemIds; // 체크박스로 선택한 장바구니 아이템 ID
}
