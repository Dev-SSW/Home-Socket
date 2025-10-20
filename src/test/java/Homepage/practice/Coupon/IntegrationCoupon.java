package Homepage.practice.Coupon;

import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.Coupon.DTO.CouponUpdateRequest;
import Homepage.practice.TestIntegrationInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class IntegrationCoupon {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CouponRepository couponRepository;

    @Test
    @Transactional
    @DisplayName("쿠폰 생성하기 성공")
    @WithMockUser(roles = "ADMIN")
    void createCoupon_success() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/admin/coupon/createCoupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CouponRequest("coupon1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("쿠폰 생성 성공"))
                .andExpect(jsonPath("$.data.name").value("coupon1"));
    }

    @Test
    @Transactional
    @DisplayName("쿠폰 생성하기 실패 - 이미 존재하는 쿠폰")
    @WithMockUser(roles = "ADMIN")
    void createCoupon_fail() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);

        // when & then
        mockMvc.perform(post("/admin/coupon/createCoupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CouponRequest("coupon1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 쿠폰 입니다."));
    }

    @Test
    @Transactional
    @DisplayName("모든 쿠폰 조회하기 성공")
    @WithMockUser(roles = "ADMIN")
    void getAllCoupon_success() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);

        // when & then
        mockMvc.perform(get("/admin/coupon/getAllCoupon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("모든 쿠폰 조회 성공"))
                .andExpect(jsonPath("$.data[0].name").value("coupon1"));
    }

    @Test
    @Transactional
    @DisplayName("특정 쿠폰 조회하기 성공")
    @WithMockUser(roles = "ADMIN")
    void getCoupon_success() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);

        // when & then
        mockMvc.perform(get("/admin/coupon/{couponId}/getCoupon", testCoupon.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("특정 쿠폰 조회 성공"))
                .andExpect(jsonPath("$.data.name").value("coupon1"));
    }

    @Test
    @Transactional
    @DisplayName("특정 쿠폰 조회하기 실패 - 해당 쿠폰 없음")
    @WithMockUser(roles = "ADMIN")
    void getCoupon_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/admin/coupon/{couponId}/getCoupon", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 쿠폰이 없습니다."));
    }

    @Test
    @Transactional
    @DisplayName("쿠폰 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateCoupon_success() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);

        // when & then
        mockMvc.perform(put("/admin/coupon/{couponId}/updateCoupon", testCoupon.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CouponUpdateRequest("coupon2", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("쿠폰 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("coupon2"));
    }

    @Test
    @Transactional
    @DisplayName("쿠폰 수정하기 실패 - 해당 쿠폰 없음")
    @WithMockUser(roles = "ADMIN")
    void updateCoupon_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/admin/coupon/{couponId}/updateCoupon", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CouponUpdateRequest("coupon2", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30 ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 쿠폰이 없습니다."));
    }

    @Test
    @Transactional
    @DisplayName("쿠폰 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteCoupon_success() throws Exception {
        // given
        Coupon testCoupon = TestIntegrationInit.createCoupon(couponRepository);

        // when & then
        mockMvc.perform(delete("/admin/coupon/{couponId}/deleteCoupon", testCoupon.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("쿠폰 삭제 성공"));
    }

    @Test
    @Transactional
    @DisplayName("쿠폰 삭제하기 실패 - 해당 쿠폰 없음")
    @WithMockUser(roles = "ADMIN")
    void deleteCoupon_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/admin/coupon/{couponId}/deleteCoupon", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 쿠폰이 없습니다."));
    }
}
