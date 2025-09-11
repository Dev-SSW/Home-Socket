package Homepage.practice.Category.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryUpdateRequest {
    private String name;        // 카테고리 명
    private Integer orderIndex; // 정렬용 인덱스
}
