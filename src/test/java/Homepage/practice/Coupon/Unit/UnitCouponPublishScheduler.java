package Homepage.practice.Coupon.Unit;

import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.CouponPublish.CouponPublishScheduler;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import Homepage.practice.Exception.CouponNotFound;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class UnitCouponPublishScheduler {
    @Mock
    private CouponPublishRepository couponPublishRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CouponPublishScheduler scheduler;

    private User testUser;
    private Coupon birthdayCoupon;

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
        birthdayCoupon = Coupon.createCoupon(
                new CouponRequest("생일쿠폰", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30));
    }

    @Test
    @DisplayName("쿠폰 기간 만료 설정 성공")
    void updateExpiredCoupons_success() {
        // given
        CouponPublish couponPublish = CouponPublish.createCoupon(birthdayCoupon, testUser);
        given(couponPublishRepository.findAllByStatusAndValidEndBefore(eq(CouponPublishStatus.AVAILABLE), any(LocalDate.class)))
                .willReturn(List.of(couponPublish));

        // when
        scheduler.updateExpiredCoupons();

        // then
        assertThat(couponPublish.getStatus()).isEqualTo(CouponPublishStatus.EXPIRED);
    }

    @Test
    @DisplayName("생일 쿠폰 자동 발급 성공")
    void publishBirthdayCoupons_success() {
        // given
        given(userRepository.findAllByBirth(any(LocalDate.class))).willReturn(List.of(testUser));
        given(couponRepository.findByName("생일쿠폰")).willReturn(Optional.of(birthdayCoupon));
        given(couponPublishRepository.existsByUserAndCoupon(testUser, birthdayCoupon)).willReturn(false);

        // when
        scheduler.publishBirthdayCoupons();

        // then
        verify(couponPublishRepository, times(1)).save(any(CouponPublish.class));
    }

    @Test
    @DisplayName("생일 쿠폰 자동 발급 실패 - 해당 쿠폰 이미 존재")
    void publishBirthdayCoupons_alreadyExists() {
        // given
        given(userRepository.findAllByBirth(any(LocalDate.class))).willReturn(List.of(testUser));
        given(couponRepository.findByName("생일쿠폰")).willReturn(Optional.of(birthdayCoupon));
        given(couponPublishRepository.existsByUserAndCoupon(testUser, birthdayCoupon)).willReturn(true);

        // when
        scheduler.publishBirthdayCoupons();

        // then
        verify(couponPublishRepository, never()).save(any());
    }

    @Test
    @DisplayName("생일 쿠폰 자동 발급 실패 - 해당 쿠폰 없음")
    void publishBirthdayCoupons_couponNotFound() {
        // given
        given(userRepository.findAllByBirth(any(LocalDate.class))).willReturn(List.of(testUser));
        given(couponRepository.findByName("생일쿠폰")).willReturn(Optional.empty());

        // when & then
        try {
            scheduler.publishBirthdayCoupons();
        } catch (CouponNotFound e) {
            assertThat(e.getMessage()).isEqualTo("생일 쿠폰이 존재하지 않습니다.");
        }
    }
}
