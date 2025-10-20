package Homepage.practice.Coupon;

import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.TestIntegrationInit;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class IntegrationCouponPublish {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CouponPublishRepository couponPublishRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestIntegrationInit.createUser(userRepository);
    }

    @Test
    @Transactional
    @DisplayName("특정 유저에게 쿠폰 발급하기 성공")
    void publishCoupon_success() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);

        // when & then
        mockMvc.perform(post("/user/coupon/{couponId}/couponPublish/publishCoupon", testCoupon.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("쿠폰 발급 성공"))
                .andExpect(jsonPath("$.data.couponName").value("coupon1"));
    }

    @Test
    @Transactional
    @DisplayName("특정 유저에게 쿠폰 발급하기 실패 - 해당 쿠폰 이미 존재")
    void publishCoupon_fail() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);
        CouponPublish testCouponPublish = TestIntegrationInit.createCouponPublish(couponPublishRepository, testCoupon, testUser);

        // when & then
        mockMvc.perform(post("/user/coupon/{couponId}/couponPublish/publishCoupon", testCoupon.getId())
                        .with(user(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 발급받은 쿠폰 입니다."));
    }

    @Test
    @Transactional
    @DisplayName("유저의 쿠폰 조회 성공")
    void getCouponPublish_success() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);
        CouponPublish testCouponPublish = TestIntegrationInit.createCouponPublish(couponPublishRepository, testCoupon, testUser);

        // when & then
        mockMvc.perform(get("/user/coupon/couponPublish/getCouponPublish")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("쿠폰 조회 성공"))
                .andExpect(jsonPath("$.data[0].couponName").value("coupon1"));
    }
}
