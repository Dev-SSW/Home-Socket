package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.DTO.AddressResponse;
import Homepage.practice.Delivery.DTO.AddressUpdateRequest;
import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Address", description = "주소 관리 API")
@RestController
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    
    @PostMapping("/user/address/createAddress")
    @Operation(summary = "주소 생성")
    public ResponseEntity<GlobalApiResponse<AddressResponse>> createAddress(@AuthenticationPrincipal User user,
                                                                            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(user.getId(), request);
        return ResponseEntity.ok(GlobalApiResponse.success("주소 생성 성공", response));
    }

    @GetMapping("/user/address/getAllAddress")
    @Operation(summary = "유저의 전체 주소 조회")
    public ResponseEntity<GlobalApiResponse<List<AddressResponse>>> getAllAddress(@AuthenticationPrincipal User user) {
        List<AddressResponse> responses = addressService.getAllAddress(user.getId());
        return ResponseEntity.ok(GlobalApiResponse.success("전체 주소 조회 성공", responses));
    }

    @GetMapping("/user/address/{addressId}/getAddress")
    @Operation(summary = "유저의 특정 주소 조회")
    public ResponseEntity<GlobalApiResponse<AddressResponse>> getAddress(@PathVariable(name = "addressId") Long addressId) {
        AddressResponse response = addressService.getAddress(addressId);
        return ResponseEntity.ok(GlobalApiResponse.success("특정 주소 조회 성공", response));
    }

    @PutMapping("/user/address/{addressId}/updateAddress")
    @Operation(summary = "주소 업데이트")
    public ResponseEntity<GlobalApiResponse<AddressResponse>> updateAddress(@AuthenticationPrincipal User user,
                                                                            @PathVariable(name = "addressId") Long addressId,
                                                                            @Valid @RequestBody AddressUpdateRequest request) {
        AddressResponse response = addressService.updateAddress(user.getId(), addressId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("주소 업데이트 성공", response));
    }

    @DeleteMapping("/user/address/{addressId}/deleteAddress")
    @Operation(summary = "주소 삭제")
    public ResponseEntity<GlobalApiResponse<AddressResponse>> deleteAddress(@AuthenticationPrincipal User user,
                                                                            @PathVariable(name = "addressId") Long addressId) {
        addressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.ok(GlobalApiResponse.success("주소 삭제 성공", null));
    }

    @PutMapping("/user/address/{addressId}/updateDefault")
    @Operation(summary = "기본 배송지 변경")
    public ResponseEntity<GlobalApiResponse<AddressResponse>> updateDefault(@AuthenticationPrincipal User user,
                                                                            @PathVariable(name = "addressId") Long addressId) {
        addressService.updateDefault(user.getId(), addressId);
        return ResponseEntity.ok(GlobalApiResponse.success("기본 배송지 변경 성공", null));
    }
}
