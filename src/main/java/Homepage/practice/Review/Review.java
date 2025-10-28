package Homepage.practice.Review;

import Homepage.practice.Item.Item;
import Homepage.practice.Review.DTO.ReviewRequest;
import Homepage.practice.Review.DTO.ReviewUpdateRequest;
import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "review_id")
    private Long id;
    private String title;           // 제목
    private String comment;         // 내용
    private float star;             // 별점
    private LocalDate reviewDate;   // 리뷰 날짜

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id")
    private Item item;

    /** 연관관계 편의 메서드 */
    public void setUser(User user) { this.user = user; }

    public void setItem(Item item) { this.item = item; }

    /** 생성 메서드 */
    public static Review createReview(User user, Item item, ReviewRequest request, int reviewCount) {
        Review review = Review.builder()
                .title(request.getTitle())
                .comment(request.getComment())
                .star(request.getStar())
                .reviewDate(LocalDate.now())
                .build();
        item.updateAvgStar(request.getStar(), reviewCount);
        item.addReview(review);
        user.addReview(review);
        return  review;
    }

    /** 수정 메서드 */
    public Review updateReview(ReviewUpdateRequest request, int reviewCount) {
        if (request.getTitle() != null) {
            this.title = request.getTitle();
        }
        if (request.getComment() != null) {
            this.comment = request.getComment();
        }
        if (request.getStar() != null) {
            this.star = request.getStar();
            item.updateAvgStar(request.getStar(), reviewCount);
        }
        return this;
    }
}
