package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.DTO.AddressResponse;
import Homepage.practice.Delivery.DTO.AddressUpdateRequest;
import Homepage.practice.Exception.AddressNotFound;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /** 주소 생성 */
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));

        // 유저의 기본 배송지 확인
        Address currentDefault = addressRepository.findByUserIdAndDefaultAddressTrue(userId);

        // 등록하려는 배송지가 기본 배송지 (true)로 입력된다면
        if (request.isDefaultAddress()) {
            // 기존의 기본 배송지를 해제
            if (currentDefault != null) {
                currentDefault.setDefaultAddress(false);
            }
        } else {
            // 기본 배송지가 없는 상황 / 유저의 첫 주소라면 자동으로 기본 배송지 설정
            if (addressRepository.countByUser(user) == 0) {
                request = new AddressRequest(
                        request.getStreet(),
                        request.getDetailStreet(),
                        request.getZipcode(),
                        true);
            }
        }
        Address address = Address.createAddress(user, request);
        addressRepository.save(address);
        return AddressResponse.fromEntity(address);
    }

    /** 유저의 전체 주소 조회 */
    public List<AddressResponse> getAllAddress(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponse::fromEntity)
                .toList();
    }

    /** 유저의 특정 주소 조회 */
    public AddressResponse getAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFound("아이디에 해당하는 주소를 찾을 수 없습니다."));
        return AddressResponse.fromEntity(address);
    }

    /** 주소 업데이트 */
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressUpdateRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFound("아이디에 해당하는 주소를 찾을 수 없습니다."));

        // 기본 배송지 로직 반영
        Address currentDefault = addressRepository.findByUserIdAndDefaultAddressTrue(userId);
        if (request.isDefaultAddress()) {
            if (currentDefault != null && !currentDefault.getId().equals(addressId)) {
                currentDefault.setDefaultAddress(false);
            }
            address.setDefaultAddress(true);
        }

        address.updateAddress(request);

        return AddressResponse.fromEntity(address);
    }

    /** 주소 삭제 */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFound("아이디에 해당하는 주소를 찾을 수 없습니다."));

        // 삭제하는 배송지가 기본 배송지인지 확인하기 위함
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        // 만약 기본 배송지를 삭제했다면, 다른 주소 하나를 기본 배송지로 설정
        if (wasDefault) {
            Address firstAddress = addressRepository.findFirstByUserIdOrderByIdAsc(userId);
            if (firstAddress != null) {
                firstAddress.setDefaultAddress(true);
            }
        }
    }

    /** 기본 배송지 변경 */
    @Transactional
    public void updateDefault(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFound("아이디에 해당하는 주소를 찾을 수 없습니다."));

        // 기존 기본 배송지 해제
        Address currentDefault = addressRepository.findByUserIdAndDefaultAddressTrue(userId);
        if (currentDefault != null) {
            currentDefault.setDefaultAddress(false);
        }

        // 선택된 주소를 기본 배송지로 지정
        address.setDefaultAddress(true);
    }
}
