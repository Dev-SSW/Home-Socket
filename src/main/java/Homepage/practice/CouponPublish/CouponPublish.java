package Homepage.practice.CouponPublish;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Exception.CouponExpired;
import Homepage.practice.Exception.CouponPublishAlreadyUsed;
import Homepage.practice.Exception.CouponPublishExpired;
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

    public void setStatus(CouponPublishStatus status) {
        this.status = status;
    }

    /** 연관관계 편의 메서드 */
    public void setUser(User user) { this.user = user; }
    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    /** 생성 메서드 */
    public static CouponPublish createCoupon(Coupon coupon, User user) {
        if (LocalDate.now().isAfter(coupon.getValidEnd())) {
            throw new CouponExpired("쿠폰 발급 기한이 지났습니다.");
        }
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(coupon.getAfterIssue()); // 발급 후 N일

        CouponPublish couponPublish = CouponPublish.builder()
                .validStart(startDate)
                .validEnd(endDate)
                .status(CouponPublishStatus.AVAILABLE)
                .build();
        coupon.addCouponPublish(couponPublish);
        user.addCouponPublishes(couponPublish);
        return couponPublish;
    }

    /** 비즈니스 로직 */
    public void useCoupon() {
        if (this.status == CouponPublishStatus.USED) {
            throw new CouponPublishAlreadyUsed("이미 사용된 쿠폰입니다.");
        }
        if (this.status == CouponPublishStatus.EXPIRED || LocalDate.now().isAfter(validEnd)) {
            this.status = CouponPublishStatus.EXPIRED;
            throw new CouponPublishExpired("쿠폰 사용 기한이 지났습니다.");
        }
        this.status = CouponPublishStatus.USED;
    }
}
