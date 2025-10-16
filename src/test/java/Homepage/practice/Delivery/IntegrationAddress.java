package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.DTO.AddressUpdateRequest;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Rollback
public class IntegrationAddress {
    // 테스트 인프라
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setup() {
        testUser = TestIntegrationInit.createUser(userRepository);
    }

    @Test
    @DisplayName("주소 생성 성공 - 기본 배송지 존재하는데 true로 들어온 상황")
    void createAddress_success1() throws Exception {
        // given
        // 기본 배송지 저장
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        // 새로운 true 주소
        AddressRequest request = new AddressRequest("street2", "detailStreet2", "zipcode", true);

        // when & then
        mockMvc.perform(post("/user/address/createAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 생성 성공"))
                .andExpect(jsonPath("$.data.street").value("street2"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));

        // 기존 기본 배송지가 false 처리 되었는지 확인
        Address defaultAddress = addressRepository.findById(testAddress.getId()).orElseThrow();
        assertThat(defaultAddress.isDefaultAddress()).isFalse();
    }

    @Test
    @DisplayName("주소 생성 성공 - 기본 배송지 없는데 첫 주소는 아닐 때")
    void createAddress_success2() throws Exception {
        // given
        // 첫 번째 false 주소
        AddressRequest request1 = new AddressRequest("street1", "detailStreet2", "zipcode", false);
        Address address = Address.createAddress(testUser, request1);
        addressRepository.save(address);
        // 두 번째 false 주소
        AddressRequest request2 = new AddressRequest("street1", "detailStreet2", "zipcode", false);

        // when & then
        mockMvc.perform(post("/user/address/createAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 생성 성공"));

        // 유저에게 true인 주소가 없는지 확인
        Address defaultAddress = addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId());
        assertThat(defaultAddress).isNull();
    }

    @Test
    @DisplayName("주소 생성 성공 - 기본 배송지 없는데 첫 주소일 때")
    void createAddress_success3() throws Exception {
        // given
        // 새로운 false 주소
        AddressRequest request = new AddressRequest("street2", "detailStreet2", "zipcode", false);

        // when & then
        mockMvc.perform(post("/user/address/createAddress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 생성 성공"))
                .andExpect(jsonPath("$.data.street").value("street2"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));
    }

    @Test
    @DisplayName("유저의 전체 주소 조회 성공")
    void getAllAddress_success() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);

        // when & then
        mockMvc.perform(get("/user/address/getAllAddress")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 주소 조회 성공"))
                .andExpect(jsonPath("$.data[0].street").value("street1"));
    }

    @Test
    @DisplayName("유저의 특정 주소 조회 성공")
    void getAddress_success() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);

        // when & then
        mockMvc.perform(get("/user/address/{addressId}/getAddress", testAddress.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("특정 주소 조회 성공"))
                .andExpect(jsonPath("$.data.street").value("street1"));
    }

    @Test
    @DisplayName("유저의 특정 주소 조회 실패 - 아이디에 해당하는 주소를 찾을 수 없습니다")
    void getAddress_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/user/address/{addressId}/getAddress", 10L)
                    .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 주소를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("주소 업데이트 성공 - 기본 배송지 존재하는데 true로 들어온 상황")
    void updateAddress_success1() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        // 새로운 주소 하나 더 넣어두기
        AddressRequest request = new AddressRequest("street2", "detailStreet2", "zipcode", false);
        Address address2 = Address.createAddress(testUser, request);
        addressRepository.save(address2);
        // 수정 입력값이 true
        AddressUpdateRequest updateRequest = new AddressUpdateRequest("street3", "detailStreet3", "zipcode", true);

        // when & then
        mockMvc.perform(put("/user/address/{addressId}/updateAddress", address2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 업데이트 성공"))
                .andExpect(jsonPath("$.data.street").value("street3"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));

        // 기존 기본 배송지가 false 처리 되었는지 확인
        Address defaultAddress = addressRepository.findById(testAddress.getId()).orElseThrow();
        assertThat(defaultAddress.isDefaultAddress()).isFalse();
    }

    @Test
    @DisplayName("주소 업데이트 성공 - 수정 할 배송지가 기본 배송지인 경우")
    void updateAddress_success2() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);

        // 수정 입력값이 true
        AddressUpdateRequest updateRequest = new AddressUpdateRequest("street3", "detailStreet3", "zipcode", true);

        // when & then
        mockMvc.perform(put("/user/address/{addressId}/updateAddress", testAddress.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 업데이트 성공"))
                .andExpect(jsonPath("$.data.street").value("street3"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));
    }

    @Test
    @DisplayName("주소 업데이트 실패 - 아이디에 해당하는 주소를 찾을 수 없습니다")
    void updateAddress_fail() throws Exception {
        // given
        AddressUpdateRequest updateRequest = new AddressUpdateRequest("street3", "detailStreet3", "zipcode", true);

        // when & then
        mockMvc.perform(put("/user/address/{addressId}/updateAddress", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 주소를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("주소 삭제하기 성공 - 삭제하는 배송지가 기본 배송지가 아닐 때")
    void deleteAddress_success1() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        // 새로운 주소 하나 더 넣어두기
        AddressRequest request = new AddressRequest("street2", "detailStreet2", "zipcode", false);
        Address address2 = Address.createAddress(testUser, request);
        addressRepository.save(address2);

        // when & then
        mockMvc.perform(delete("/user/address/{addressId}/deleteAddress", address2.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 삭제 성공"));

        // 기존 기본 배송지가 잘 있는지 확인
        Address defaultAddress = addressRepository.findById(testAddress.getId()).orElseThrow();
        assertThat(defaultAddress.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("주소 삭제하기 성공 - 삭제하는 배송지가 기본 배송지 일 때")
    void deleteAddress_success2() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        // 새로운 주소 하나 더 넣어두기
        AddressRequest request = new AddressRequest("street2", "detailStreet2", "zipcode", false);
        Address address2 = Address.createAddress(testUser, request);
        addressRepository.save(address2);

        // when & then
        mockMvc.perform(delete("/user/address/{addressId}/deleteAddress", testAddress.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주소 삭제 성공"));

        // 나머지 false 배송지가 true로 바뀌었는지 확인
        Address defaultAddress = addressRepository.findById(address2.getId()).orElseThrow();
        assertThat(defaultAddress.getStreet()).isEqualTo("street2");
        assertThat(defaultAddress.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("주소 삭제하기 실패 - 아이디에 해당하는 주소를 찾을 수 없습니다")
    void deleteAddress_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/user/address/{addressId}/deleteAddress", 10L)
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 주소를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("기본 배송지 변경하기 성공")
    void updateDefault_success() throws Exception {
        // given
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        // 새로운 주소 하나 더 넣어두기
        AddressRequest request = new AddressRequest("street2", "detailStreet2", "zipcode", false);
        Address address2 = Address.createAddress(testUser, request);
        addressRepository.save(address2);

        // when & then
        mockMvc.perform(put("/user/address/{addressId}/updateDefault", address2.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("기본 배송지 변경 성공"));

        // alse 배송지가 true로 바뀌었는지 확인
        Address defaultAddress = addressRepository.findById(address2.getId()).orElseThrow();
        assertThat(defaultAddress.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 변경하기 실패 - 아이디에 해당하는 주소를 찾을 수 없습니다")
    void updateDefault_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/user/address/{addressId}/updateDefault", 10L)
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 주소를 찾을 수 없습니다."));
    }
}
