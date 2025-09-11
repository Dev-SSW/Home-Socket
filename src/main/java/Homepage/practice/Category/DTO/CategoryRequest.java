package Homepage.practice.Category.DTO;

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
public class CategoryRequest {
    @NotBlank(message = "카테고리 이름을 입력하셔야 합니다.")
    private String name;        // 카테고리 명
    @NotNull(message = "트리 전체에서 몇 단계 아래인지를 나타내는 depth를 입력하셔야 합니다.")
    private int depth;          // 루트 = 0, 하위 = 1, 2, ...
    @NotNull(message = "같은 parent를 가진 같은 depth에서, 순서를 나타내는 orderIndex를 입력하셔야 합니다.")
    private int orderIndex;     // 정렬용 인덱스
    private Long parentId;      // 부모 카테고리 id
}
