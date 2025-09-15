package Homepage.practice.Item.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemUpdateRequest {
    private String name;            // 상품명
    private Integer stock;          // 상품 재고
    private Integer itemPrice;      // 상품 가격
}
