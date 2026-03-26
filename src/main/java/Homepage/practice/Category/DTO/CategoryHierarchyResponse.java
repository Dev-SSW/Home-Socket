package Homepage.practice.Category.DTO;

import Homepage.practice.Category.Category;
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
public class CategoryHierarchyResponse {
    private Long id;
    private String name;
    private int depth;
    private int orderIndex;
    private List<CategoryHierarchyResponse> children;

    public static CategoryHierarchyResponse fromEntity(Category category) {
        return CategoryHierarchyResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .depth(category.getDepth())
                .orderIndex(category.getOrderIndex())
                .children(category.getChildren().stream()
                        .map(CategoryHierarchyResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
