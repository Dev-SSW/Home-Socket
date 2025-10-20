package Homepage.practice.Coupon;

import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.Coupon.DTO.CouponResponse;
import Homepage.practice.Coupon.DTO.CouponUpdateRequest;
import Homepage.practice.TestUnitInit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitCoupon {
    @Mock private CouponRepository couponRepository;
    @InjectMocks private CouponService couponService;

    @Test
    @DisplayName("쿠폰 만들기 성공")
    void createCoupon_success() {
        // given
        CouponRequest request = new CouponRequest("coupon1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 90 );
        given(couponRepository.existsByName(request.getName())).willReturn(false);

        // when
        CouponResponse response = couponService.createCoupon(request);

        // then
        verify(couponRepository).save(any(Coupon.class));
        assertThat(response.getName()).isEqualTo("coupon1");
    }

    @Test
    @DisplayName("쿠폰 수정 성공")
    void updateCoupon_success() {
        // given
        Coupon testCoupon = TestUnitInit.createCoupon(1L);
        given(couponRepository.findById(testCoupon.getId())).willReturn(Optional.of(testCoupon));

        // when
        CouponResponse response = couponService.updateCoupon(testCoupon.getId(),
                new CouponUpdateRequest("coupon2", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30));

        // then
        assertThat(response.getName()).isEqualTo("coupon2");
    }

    @Test
    @DisplayName("쿠폰 삭제 성공")
    void deleteCoupon_success() {
        // given
        Coupon testCoupon = TestUnitInit.createCoupon(1L);
        given(couponRepository.findById(testCoupon.getId())).willReturn(Optional.of(testCoupon));

        // when
        couponService.deleteCoupon(testCoupon.getId());

        // then
        verify(couponRepository).delete(any(Coupon.class));
    }
}
