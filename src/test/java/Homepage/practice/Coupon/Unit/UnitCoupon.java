package Homepage.practice.Coupon.Unit;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.Coupon.CouponService;
import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.Coupon.DTO.CouponResponse;
import Homepage.practice.Coupon.DTO.CouponUpdateRequest;
import Homepage.practice.Exception.CouponAlreadyExists;
import Homepage.practice.Exception.CouponNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitCoupon {
    @Mock
    private CouponRepository couponRepository;
    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("쿠폰 만들기 성공")
    void createCoupon_success() {
        // given
        CouponRequest request = new CouponRequest("생일쿠폰", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 );
        given(couponRepository.existsByName(request.getName())).willReturn(false);
        // when
        CouponResponse response = couponService.createCoupon(request);
        // then
        assertThat(request.getName()).isEqualTo(response.getName());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("쿠폰 만들기 실패 - 이미 존재하는 쿠폰")
    void createCoupon_fail() {
        //given
        CouponRequest request = new CouponRequest("생일쿠폰", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 );
        given(couponRepository.existsByName(request.getName())).willReturn(true);
        //when & then
        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(CouponAlreadyExists.class)
                .hasMessage("이미 존재하는 쿠폰 입니다.");
    }

    @Test
    @DisplayName("모든 쿠폰 조회 성공")
    void getAllCoupon_success() {
        //given
        Coupon coupon1 = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        Coupon coupon2 = Coupon.createCoupon(new CouponRequest("생일쿠폰2", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findAll()).willReturn(Arrays.asList(coupon1, coupon2));
        //when
        List<CouponResponse> responses = couponService.getAllCoupon();
        //then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("생일쿠폰1");
        assertThat(responses.get(1).getName()).isEqualTo("생일쿠폰2");
    }

    @Test
    @DisplayName("특정 쿠폰 조회 성공")
    void getCoupon_success() {
        //given
        Coupon coupon1 = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findById(1L)).willReturn(Optional.of(coupon1));
        //when
        CouponResponse response = couponService.getCoupon(1L);
        //then
        assertThat(response.getName()).isEqualTo("생일쿠폰1");
    }

    @Test
    @DisplayName("특정 쿠폰 조회 실패 - 해당 쿠폰 없음")
    void getCoupon_fail() {
        //given
        Coupon coupon1 = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findById(1L)).willReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> couponService.getCoupon(1L))
                .isInstanceOf(CouponNotFound.class)
                .hasMessage("아이디에 해당하는 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("쿠폰 수정 성공")
    void updateCoupon_success() {
        //given
        CouponUpdateRequest request = new CouponUpdateRequest("새_생일쿠폰", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30);
        Coupon coupon1 = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findById(1L)).willReturn(Optional.of(coupon1));
        //when
        CouponResponse response = couponService.updateCoupon(1L, request);
        //then
        assertThat(response.getName()).isEqualTo("새_생일쿠폰");
    }

    @Test
    @DisplayName("쿠폰 수정 실패 - 해당 쿠폰 없음")
    void updateCoupon_fail() {
        //given
        CouponUpdateRequest request = new CouponUpdateRequest("새_생일쿠폰", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30);
        given(couponRepository.findById(1L)).willReturn(Optional.empty());
        //when & then
        assertThatThrownBy(() -> couponService.updateCoupon(1L, request))
                .isInstanceOf(CouponNotFound.class)
                .hasMessage("아이디에 해당하는 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("쿠폰 삭제 성공")
    void deleteCoupon_success() {
        //given
        Coupon coupon1 = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findById(1L)).willReturn(Optional.of(coupon1));
        //when
        couponService.deleteCoupon(1L);
        //then
        verify(couponRepository).delete(any(Coupon.class));
    }
}
