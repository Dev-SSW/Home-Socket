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
        // 존재 여부만 확인 (불필요한 전체 객체 조회 방지)
        if (!couponRepository.existsById(couponId)) {
            throw new CouponNotFound("아이디에 해당하는 쿠폰이 없습니다.");
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound("아이디에 해당하는 회원이 없습니다.");
        }
        
        // ID 기반으로 중복 발급 확인
        if (couponPublishRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new CouponPublishAlreadyExist("이미 발급받은 쿠폰 입니다.");
        }

        // 생성에 필요한 객체 Proxy 사용
        Coupon coupon = couponRepository.getReferenceById(couponId);
        User user = userRepository.getReferenceById(userId);
        
        CouponPublish couponPublish = CouponPublish.createCoupon(coupon, user);
        couponPublishRepository.save(couponPublish);
        return CouponPublishResponse.fromEntity(couponPublish);
    }

    /** 유저의 쿠폰 조회 */
    public List<CouponPublishResponse> getCouponPublish(User user) {
        // DTO Projection으로 바로 조회
        return couponPublishRepository.findCouponPublishByUserId(user.getId());
    }
}
