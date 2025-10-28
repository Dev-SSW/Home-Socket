package Homepage.practice.Review.DTO;

import Homepage.practice.Review.Review;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {
    private Long id;
    private String title;           // 제목
    private String comment;         // 내용
    private float star;             // 별점
    private LocalDate reviewDate;   // 리뷰 날짜

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .title(review.getTitle())
                .comment(review.getComment())
                .star(review.getStar())
                .reviewDate(review.getReviewDate())
                .build();
    }
}
