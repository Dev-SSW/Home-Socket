package Homepage.practice.Review.DTO;

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
public class ReviewRequest {
    @NotBlank(message = "리뷰의 제목을 입력하셔야 합니다.")
    private String title;           // 제목
    private String comment;         // 내용
    @NotNull(message = "별점을 입력하셔야 합니다.")
    private float star;             // 별점
}
