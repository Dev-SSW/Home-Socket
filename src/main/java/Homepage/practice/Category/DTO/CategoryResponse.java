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
public class CategoryResponse {
    private Long id;            // id
    private String name;        // 카테고리 명
    private int depth;          // 루트 = 0, 하위 = 1, 2, ...
    private int orderIndex;     // 정렬용 인덱스
    private Long parentId;      // 부모 카테고리 id
    private List<Long> childrenIds;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .depth(category.getDepth())
                .orderIndex(category.getOrderIndex())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .childrenIds(category.getChildren() != null ?
                        category.getChildren().stream().map(Category::getId).collect(Collectors.toList()) : List.of())
                .build();
    }
}
