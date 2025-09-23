package Homepage.practice.CouponPublish;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.CouponPublish.DTO.CouponPublishResponse;
import Homepage.practice.Exception.CouponNotFound;
import Homepage.practice.Exception.CouponPublishAlreadyExist;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponPublishService {
    private final CouponPublishRepository couponPublishRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    /** 특정 유저에게 쿠폰 발급 */
    @Transactional
    public CouponPublishResponse publishCoupon(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFound("아이디에 해당하는 쿠폰이 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        if (couponPublishRepository.existsByUserAndCoupon(user, coupon)) {
            throw new CouponPublishAlreadyExist("이미 발급받은 쿠폰 입니다.");
        }

        CouponPublish couponPublish = CouponPublish.createCoupon(coupon, user);
        couponPublishRepository.save(couponPublish);
        return CouponPublishResponse.fromEntity(couponPublish);
    }

    /** 유저의 쿠폰 조회 */
    public List<CouponPublishResponse> getCouponPublish(User user) {
        List<CouponPublish> couponPublishes = couponPublishRepository.findByUser(user);
        return couponPublishes.stream()
                .map(CouponPublishResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
