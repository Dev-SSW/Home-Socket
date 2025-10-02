package Homepage.practice.Delivery.DTO;

import Homepage.practice.Delivery.Address;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {
    private Long id;
    private String street;          // 도로명
    private String detailStreet;    // 상세 주소
    private String zipcode;         // 우편번호
    private boolean defaultAddress; // 기본 배송지 여부

    public static AddressResponse fromEntity(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .detailStreet(address.getDetailStreet())
                .zipcode(address.getZipcode())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}
