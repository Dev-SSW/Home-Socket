package Homepage.practice.Review;

import Homepage.practice.Item.Item;
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
}
