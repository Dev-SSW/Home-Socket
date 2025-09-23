package Homepage.practice.Coupon.DTO;

import Homepage.practice.Coupon.Coupon;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponse {
    private Long id;
    private String name;            // 쿠폰 명
    private BigDecimal discount;    // 할인 가격
    private LocalDate validStart;   // 쿠폰 자체 사용 기한 (고정형 시작)
    private LocalDate validEnd;     // 쿠폰 자체 사용 기한 (고정형 만료)
    private Integer afterIssue;   // 유저 발급 후 유효 기간 (상대형 만료)

    public static CouponResponse fromEntity(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .discount(coupon.getDiscount())
                .validStart(coupon.getValidStart())
                .validEnd(coupon.getValidEnd())
                .afterIssue(coupon.getAfterIssue())
                .build();
    }
}
