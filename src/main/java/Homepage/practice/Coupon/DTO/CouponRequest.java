package Homepage.practice.Coupon.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponRequest {
    @NotBlank(message = "쿠폰 이름을 입력하셔야 합니다.")
    private String name;            // 쿠폰 명
    @NotNull(message = "할인 가격을 입력하셔야 합니다.")
    private BigDecimal discount;    // 할인 가격
    @NotNull(message = "쿠폰 사용 기한 (시작)을 입력하셔야 합니다.") @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;   // 쿠폰 자체 사용 기한 (고정형 시작)
    @NotNull(message = "쿠폰 사용 기한 (끝)을 입력하셔야 합니다.") @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;     // 쿠폰 자체 사용 기한 (고정형 만료)
    @NotNull(message = "쿠폰 발급 시 사용 기한을 입력하셔야 합니다.")
    private Integer afterIssue;   // 유저 발급 후 유효 기간 (상대형 만료)
}
