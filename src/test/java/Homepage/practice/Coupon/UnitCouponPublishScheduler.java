package Homepage.practice.Coupon;

import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.CouponPublish.CouponPublishScheduler;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import Homepage.practice.TestUnitInit;
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
    @Mock private CouponPublishRepository couponPublishRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CouponPublishScheduler scheduler;

    private User testUser;
    private Coupon testCoupon;

    @BeforeEach
    void setup() {
        testUser = TestUnitInit.createUser(1L);
        testCoupon = TestUnitInit.createCoupon(2L);
    }

    @Test
    @DisplayName("쿠폰 기간 만료 설정 성공")
    void updateExpiredCoupons_success() {
        // given
        CouponPublish couponPublish = TestUnitInit.createCouponPublish(3L, testCoupon, testUser);
        given(couponPublishRepository
                .findAllByStatusAndValidEndBefore(eq(CouponPublishStatus.AVAILABLE), any(LocalDate.class)))
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
        Coupon testBirthCoupon = Coupon.createCoupon(
                new CouponRequest("생일쿠폰", BigDecimal.valueOf(1000), LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1), 90));
        given(userRepository.findAllByBirthToday()).willReturn(List.of(testUser));
        given(couponRepository.findByName("생일쿠폰")).willReturn(Optional.of(testBirthCoupon));
        given(couponPublishRepository.existsByUserAndCoupon(testUser, testBirthCoupon)).willReturn(false);

        // when
        scheduler.publishBirthdayCoupons();

        // then
        verify(couponPublishRepository).save(any(CouponPublish.class));
    }
}
