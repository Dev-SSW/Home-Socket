package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.DeliveryRequest;
import Homepage.practice.Order.Order;
import Homepage.practice.Order.OrderRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class IntegrationDelivery {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;

    private Delivery testDelivery;

    @BeforeEach
    void setup() {
        User testUser = TestIntegrationInit.createUser(userRepository);
        Address testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        Order testOrder = TestIntegrationInit.createOrder(orderRepository, testUser, null, new BigDecimal("10000"));
        testDelivery = TestIntegrationInit.createDelivery(deliveryRepository, testOrder, testAddress);
    }

    @Test
    @Transactional
    @DisplayName("배송 상태 변경 성공")
    @WithMockUser(roles = "ADMIN")
    void updateDeliveryStatus_success() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/admin/delivery/{deliveryId}/updateDeliveryStatus", testDelivery.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeliveryRequest(DeliveryStatus.SHIPPING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("배송 상태 변경 성공"));

        Delivery updated = deliveryRepository.findById(testDelivery.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.SHIPPING);
    }

    @Test
    @Transactional
    @DisplayName("배송 상태 변경 실패 - 아이디에 해당하는 배송이 없습니다")
    @WithMockUser(roles = "ADMIN")
    void updateDeliveryStatus_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/admin/delivery/{deliveryId}/updateDeliveryStatus", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeliveryRequest(DeliveryStatus.SHIPPING))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 배송이 없습니다."));
    }
}
