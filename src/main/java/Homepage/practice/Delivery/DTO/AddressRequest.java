package Homepage.practice.Delivery.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressRequest {
    @NotBlank(message = "도로명을 입력하셔야 합니다.")
    private String street;          // 도로명
    @NotBlank(message = "상세 주소를 입력하셔야 합니다.")
    private String detailStreet;    // 상세 주소
    @NotBlank(message = "우편번호를 입력하셔야 합니다.")
    private String zipcode;         // 우편번호
    private boolean defaultAddress;      // 기본 배송지 여부
}
