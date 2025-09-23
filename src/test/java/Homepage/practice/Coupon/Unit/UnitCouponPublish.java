package Homepage.practice.Coupon.Unit;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.CouponPublish.CouponPublishService;
import Homepage.practice.CouponPublish.DTO.CouponPublishResponse;
import Homepage.practice.Exception.CouponPublishAlreadyExist;
import Homepage.practice.User.Role;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class UnitCouponPublish {
    @Mock
    private CouponPublishRepository couponPublishRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CouponPublishService couponPublishService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .password("pass1")
                .birth(LocalDate.of(2000, 1, 1))
                .name("홍길동")
                .role(Role.ROLE_USER)
                .tokenVersion(1)
                .build();
    }

    @Test
    @DisplayName("특정 유저에게 쿠폰 발급 성공")
    void publishCoupon_success() {
        //given
        Coupon coupon = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(couponPublishRepository.existsByUserAndCoupon(testUser, coupon)).willReturn(false);
        //when
        CouponPublishResponse response = couponPublishService.publishCoupon(1L, 1L);
        //then
        verify(couponPublishRepository).save(any(CouponPublish.class));
        assertThat(response.getCouponName()).isEqualTo("생일쿠폰1");
    }

    @Test
    @DisplayName("특정 유저에게 쿠폰 발급 실패 - 해당 쿠폰 이미 존재")
    void publishCoupon_fail() {
        //given
        Coupon coupon = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(couponPublishRepository.existsByUserAndCoupon(testUser, coupon)).willReturn(true);
        //when & then
        assertThatThrownBy(() -> couponPublishService.publishCoupon(1L, 1L))
                .isInstanceOf(CouponPublishAlreadyExist.class)
                .hasMessage("이미 발급받은 쿠폰 입니다.");
    }

    @Test
    @DisplayName("유저의 쿠폰 조회 성공")
    void getCouponPublish_success() {
        //given
        Coupon coupon1 = Coupon.createCoupon(new CouponRequest("생일쿠폰1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        Coupon coupon2 = Coupon.createCoupon(new CouponRequest("생일쿠폰2", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ));
        CouponPublish couponPublish1 = CouponPublish.createCoupon(coupon1, testUser);
        CouponPublish couponPublish2 = CouponPublish.createCoupon(coupon2, testUser);
        given(couponPublishRepository.findByUser(testUser)).willReturn(Arrays.asList(couponPublish1, couponPublish2));
        //when
        List<CouponPublishResponse> response = couponPublishService.getCouponPublish(testUser);
        //then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getCouponName()).isEqualTo("생일쿠폰1");
        assertThat(response.get(1).getCouponName()).isEqualTo("생일쿠폰2");
    }
}
