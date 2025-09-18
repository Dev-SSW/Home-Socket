package Homepage.practice.CartItem.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemRequest {
    @NotNull(message = "상품 ID를 입력하셔야 합니다.")
    private Long itemId;
    @NotNull(message = "수량을 입력하셔야 합니다.")
    @Positive(message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}
