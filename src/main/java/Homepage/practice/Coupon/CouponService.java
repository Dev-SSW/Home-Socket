package Homepage.practice.Coupon;

import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.Coupon.DTO.CouponResponse;
import Homepage.practice.Coupon.DTO.CouponUpdateRequest;
import Homepage.practice.Exception.CouponAlreadyExists;
import Homepage.practice.Exception.CouponNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    /** 쿠폰 만들기 */
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if(couponRepository.exsistByName(request.getName())) {
            throw new CouponAlreadyExists("이미 존재하는 쿠폰 입니다.");
        }
        Coupon coupon = Coupon.createCoupon(request);
        couponRepository.save(coupon);
        return CouponResponse.fromEntity(coupon);
    }

    /** 모든 쿠폰 조회 */
    public List<CouponResponse> getAllCoupon() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 특정 쿠폰 조회 */
    public CouponResponse getCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFound("아이디에 해당하는 쿠폰이 없습니다."));
        return CouponResponse.fromEntity(coupon);
    }

    /** 쿠폰 수정 */
    @Transactional
    public CouponResponse updateCoupon(Long couponId, CouponUpdateRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFound("아이디에 해당하는 쿠폰이 없습니다."));
        coupon.updateCoupon(request);
        return CouponResponse.fromEntity(coupon);
    }

    /** 쿠폰 삭제 */
    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFound("아이디에 해당하는 쿠폰이 없습니다."));
        couponRepository.delete(coupon);
    }
}
