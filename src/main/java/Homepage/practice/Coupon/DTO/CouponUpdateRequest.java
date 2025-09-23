package Homepage.practice.Coupon.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponUpdateRequest {
    private String name;            // 쿠폰 명
    private BigDecimal discount;    // 할인 가격
    private LocalDate validStart;   // 쿠폰 자체 사용 기한 (고정형 시작)
    private LocalDate validEnd;     // 쿠폰 자체 사용 기한 (고정형 만료)
    private Integer afterIssue;   // 유저 발급 후 유효 기간 (상대형 만료)
}
