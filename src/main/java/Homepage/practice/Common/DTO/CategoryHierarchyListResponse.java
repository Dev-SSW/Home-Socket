package Homepage.practice.Common.DTO;

import Homepage.practice.Category.DTO.CategoryHierarchyResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 리스트가 아닌 객체 타입으로 만들기 위해 감싸기
public class CategoryHierarchyListResponse {
    private List<CategoryHierarchyResponse> categories;
}
