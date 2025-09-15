package Homepage.practice.CouponPublish;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponPublish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "coupon_publish_id")
    private Long id;
    private LocalDate validStart;        // 쿠폰 사용 가능 기한 (시작)
    private LocalDate validEnd;          // 쿠폰 사용 가능 기한 (만료)
    @Enumerated(EnumType.STRING)
    private CouponPublishStatus status;  // AVAILABLE, USED, EXPIRED

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    /** 연관관계 편의 메서드 */
    public void setUser(User user) { this.user = user; }
    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}
