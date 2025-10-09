package Homepage.practice.Delivery.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressUpdateRequest {
    private String street;          // 도로명
    private String detailStreet;    // 상세 주소
    private String zipcode;         // 우편번호
    private boolean defaultAddress;      // 기본 배송지 여부
}
