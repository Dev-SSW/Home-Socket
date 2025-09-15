package Homepage.practice.Item.DTO;

import Homepage.practice.Item.Item;
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
public class ItemResponse {
    private Long id;
    private String name;        // 상품명
    private int stock;          // 상품 재고
    private int itemPrice;      // 상품 가격
    private float avgStar;      // 별점 평균

    public static ItemResponse fromEntity(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .itemPrice(item.getItemPrice())
                .avgStar(item.getAvgStar())
                .build();
    }
}
