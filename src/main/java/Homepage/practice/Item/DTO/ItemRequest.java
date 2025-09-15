package Homepage.practice.Item.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemRequest {
    @NotBlank(message = "상품 이름을 입력하셔야 합니다.")
    private String name;        // 상품명
    @NotNull(message = "상품 재고를 입력하셔야 합니다.")
    private int stock;          // 상품 재고
    @NotNull(message = "상품 가격을 입력하셔야 합니다.")
    private int itemPrice;      // 상품 가격
}
