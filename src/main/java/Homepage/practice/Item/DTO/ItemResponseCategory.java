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
public class ItemResponseCategory {
    private Long id;
    private String name;
    private int stock;
    private int itemPrice;
    private float avgStar;

    private Long categoryId;
    private String categoryName;

    public static ItemResponseCategory fromEntity(Item item) {
        return ItemResponseCategory.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .itemPrice(item.getItemPrice())
                .avgStar(item.getAvgStar())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .build();
    }
}
