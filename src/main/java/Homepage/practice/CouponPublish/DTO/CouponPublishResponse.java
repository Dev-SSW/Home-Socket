package Homepage.practice.CouponPublish.DTO;

import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponPublishResponse {
    private Long id;
    private LocalDate validStart;        // 쿠폰 사용 가능 기한 (시작)
    private LocalDate validEnd;          // 쿠폰 사용 가능 기한 (만료)
    private CouponPublishStatus status;  // AVAILABLE, USED, EXPIRED

    public static CouponPublishResponse fromEntity(CouponPublish couponPublish) {
        return CouponPublishResponse.builder()
                .id(couponPublish.getId())
                .validStart(couponPublish.getValidStart())
                .validEnd(couponPublish.getValidEnd())
                .status(couponPublish.getStatus())
                .build();
    }
}
