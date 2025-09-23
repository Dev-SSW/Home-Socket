package Homepage.practice.CouponPublish;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CouponPublishRepository extends JpaRepository<CouponPublish, Long> {
    /** 기간 만료된 모든 발급 쿠폰 찾기 */
    List<CouponPublish> findAllByStatusAndValidEndBefore(CouponPublishStatus status, LocalDate date);

    /** 이미 발급된 쿠폰인지 확인 */
    boolean existsByUserAndCoupon(User user, Coupon coupon);

    /** 유저로 쿠폰 찾기 */
    List<CouponPublish> findByUser(User user);
}
