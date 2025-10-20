package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.DTO.AddressResponse;
import Homepage.practice.Delivery.DTO.AddressUpdateRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitAddress {
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AddressService addressService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setup() {
        testUser = TestUnitInit.createUser(1L);
        testAddress = TestUnitInit.createAddress(2L, testUser);
    }

    @Test
    @DisplayName("주소 생성 성공 - 기본 배송지 존재하는데 true로 들어온 상황")
    void createAddress_success1() {
        // given
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId())).willReturn(testAddress);

        // when
        AddressResponse response = addressService.createAddress(testUser.getId(),
                new AddressRequest("address2", "detailStreet2", "zipcode", true));

        // then
        verify(addressRepository).save(any(Address.class));
        // 새로운 배송이 true가 되었는지
        assertThat(response.isDefaultAddress()).isTrue();
        // 기존 배송이 false가 되었는지
        assertThat(testAddress.isDefaultAddress()).isFalse();
    }

    @Test
    @DisplayName("주소 생성 성공 - 기본 배송지 없는데 첫 주소는 아닐 때")
    void createAddress_success2() {
        // given
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId())).willReturn(null);
        given(addressRepository.countByUser(testUser)).willReturn(1L);

        // when
        AddressResponse response = addressService.createAddress(testUser.getId(),
                new AddressRequest("address2", "detailStreet2", "zipcode", false));

        // then
        verify(addressRepository).save(any(Address.class));
        // 새로운 배송이 그대로 false인지
        assertThat(response.isDefaultAddress()).isFalse();
    }

    @Test
    @DisplayName("주소 생성 성공 - 기본 배송지 없는데 첫 주소일 때")
    void createAddress_success3() {
        // given
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId())).willReturn(null);
        given(addressRepository.countByUser(testUser)).willReturn(0L);

        // when
        AddressResponse response = addressService.createAddress(testUser.getId(),
                new AddressRequest("address2", "detailStreet2", "zipcode", false));

        // then
        verify(addressRepository).save(any(Address.class));
        // 새로운 배송이 첫 주소라서 true 바뀌었는지
        assertThat(response.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("주소 업데이트 성공 - 기본 배송지 존재하는데 true로 들어온 상황")
    void updateAddress_success1() {
        // given
        AddressUpdateRequest updateRequest = new AddressUpdateRequest("address3", "detailStreet3", "zipcode", true);
        // 수정 할 배송지 미리 만들기
        Address testAddress2 = Address.createAddress(testUser, new AddressRequest("address2", "detailStreet2", "zipcode", false));
        ReflectionTestUtils.setField(testAddress2, "id", 3L);
        given(addressRepository.findById(testAddress2.getId())).willReturn(Optional.of(testAddress2));
        given(addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId())).willReturn(testAddress);

        // when
        AddressResponse response = addressService.updateAddress(testUser.getId(), testAddress2.getId(), updateRequest);

        // then
        // 수정 할 배송지가 수정되었고 True인지 확인
        assertThat(response.getId()).isEqualTo(testAddress2.getId());
        assertThat(response.getStreet()).isEqualTo("address3");
        assertThat(response.isDefaultAddress()).isTrue();
        // 기존 기본 배송지가 false가 되었는지 확인
        assertThat(testAddress.getStreet()).isEqualTo("address1");
        assertThat(testAddress.isDefaultAddress()).isFalse();
    }

    @Test
    @DisplayName("주소 업데이트 성공 - 수정 할 배송지가 기본 배송지인 경우")
    void updateAddress_success2() {
        // given
        given(addressRepository.findById(testAddress.getId())).willReturn(Optional.of(testAddress));
        given(addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId())).willReturn(testAddress);

        // when
        AddressResponse response = addressService.updateAddress(testUser.getId(), testAddress.getId(),
                new AddressUpdateRequest("address3", "detailStreet3", "zipcode", true));

        // then
        // 배송지가 수정되었고 그대로 True인지 확인
        assertThat(response.getStreet()).isEqualTo("address3");
        assertThat(response.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("주소 삭제하기 성공 - 삭제하는 배송지가 기본 배송지가 아닐 때")
    void deleteAddress_success1() {
        // given
        Address testAddress2 = Address.createAddress(testUser, new AddressRequest("address2", "detailStreet2", "zipcode", false));
        ReflectionTestUtils.setField(testAddress2, "id", 3L);
        given(addressRepository.findById(testAddress2.getId())).willReturn(Optional.of(testAddress2));

        // when
        addressService.deleteAddress(testUser.getId(), testAddress2.getId());

        // then
        verify(addressRepository).delete(any(Address.class));

        // 남은 주소가 그대로 있는지 확인
        assertThat(testAddress.getStreet()).isEqualTo("address1");
        assertThat(testAddress.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("주소 삭제하기 성공 - 삭제하는 배송지가 기본 배송지 일 때")
    void deleteAddress_success2() {
        // given
        Address testAddress2 = Address.createAddress(testUser, new AddressRequest("address2", "detailStreet2", "zipcode", false));
        ReflectionTestUtils.setField(testAddress2, "id", 3L);
        given(addressRepository.findById(testAddress.getId())).willReturn(Optional.of(testAddress));
        given(addressRepository.findFirstByUserIdOrderByIdAsc(testUser.getId())).willReturn(testAddress2);

        // when
        addressService.deleteAddress(testUser.getId(), testAddress.getId());

        // then
        verify(addressRepository).delete(any(Address.class));
        // 남은 주소가 True로 바뀌었는지 확인
        assertThat(testAddress2.getStreet()).isEqualTo("address2");
        assertThat(testAddress2.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 변경 성공")
    void updateDefault_success() {
        // given
        Address testAddress2 = Address.createAddress(testUser, new AddressRequest("address2", "detailStreet2", "zipcode", false));
        ReflectionTestUtils.setField(testAddress2, "id", 3L);
        given(addressRepository.findById(testAddress2.getId())).willReturn(Optional.of(testAddress2));
        given(addressRepository.findByUserIdAndDefaultAddressTrue(testUser.getId())).willReturn(testAddress);

        // when
        addressService.updateDefault(testUser.getId(), testAddress2.getId());

        // then
        // 기존 주소가 false가 되었는지 확인
        assertThat(testAddress.isDefaultAddress()).isFalse();
        // 선택 주소가 true가 되었는지 확인
        assertThat(testAddress2.isDefaultAddress()).isTrue();
    }
}
