package Homepage.practice.Coupon;

import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.CouponPublish.CouponPublishService;
import Homepage.practice.CouponPublish.DTO.CouponPublishResponse;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitCouponPublish {
    @Mock private CouponPublishRepository couponPublishRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CouponPublishService couponPublishService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = TestUnitInit.createUser(1L);
    }

    @Test
    @DisplayName("특정 유저에게 쿠폰 발급 성공")
    void publishCoupon_success() {
        // given
        Coupon testCoupon = TestUnitInit.createCoupon(2L);
        given(couponRepository.existsById(testCoupon.getId())).willReturn(true);
        given(userRepository.existsById(testUser.getId())).willReturn(true);
        given(couponPublishRepository.existsByUserIdAndCouponId(testUser.getId(), testCoupon.getId())).willReturn(false);
        given(couponRepository.getReferenceById(testCoupon.getId())).willReturn(testCoupon);
        given(userRepository.getReferenceById(testUser.getId())).willReturn(testUser);

        // when
        CouponPublishResponse response = couponPublishService.publishCoupon(testCoupon.getId(), testUser.getId());

        // then
        verify(couponPublishRepository).save(any(CouponPublish.class));
        assertThat(response.getCouponName()).isEqualTo("coupon1");
    }
}
