package Homepage.practice.CouponPublish;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.Exception.CouponNotFound;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponPublishScheduler {
    private final CouponPublishRepository couponPublishRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    /** 매일 자정에 실행 */
    /** 쿠폰 기간 만료 설정 */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateExpiredCoupons() {
        LocalDate now = LocalDate.now();
        List<CouponPublish> expiredCoupons = couponPublishRepository
                .findAllByStatusAndValidEndBefore(CouponPublishStatus.AVAILABLE, now);
        expiredCoupons.forEach(c -> c.setStatus(CouponPublishStatus.EXPIRED));
    }

    /** 매일 새벽 1시 실행 */
    /** 생일 쿠폰 자동 발급 */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void publishBirthdayCoupons() {
        LocalDate now = LocalDate.now();

        // 오늘 생일인 유저 찾기
        List<User> birthdayUsers = userRepository.findAllByBirthToday();

        // 생일 쿠폰 가져오기 (미리 정의된 정책 쿠폰)
        Coupon birthdayCoupon = couponRepository.findByName("생일쿠폰")
                .orElseThrow(() -> new CouponNotFound("생일 쿠폰이 존재하지 않습니다."));

        for (User user : birthdayUsers) {
            // 중복 발급 방지
            boolean alreadyExists = couponPublishRepository.existsByUserAndCoupon(user, birthdayCoupon);
            if (alreadyExists) continue;

            // 발급
            CouponPublish couponPublish = CouponPublish.createCoupon(birthdayCoupon, user);
            couponPublishRepository.save(couponPublish);
        }
    }
}
