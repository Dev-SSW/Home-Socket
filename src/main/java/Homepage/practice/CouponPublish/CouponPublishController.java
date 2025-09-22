package Homepage.practice.CouponPublish;

import Homepage.practice.CouponPublish.DTO.CouponPublishResponse;
import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CouponPublish", description = "쿠폰 관리 API")
@RestController
@RequiredArgsConstructor
public class CouponPublishController {
    private final CouponPublishService couponPublishService;

    @PostMapping("/user/coupon/{couponId}/couponPublish/publishCoupon")
    @Operation(summary = "특정 유저에게 쿠폰 발급하기")
    public ResponseEntity<GlobalApiResponse<CouponPublishResponse>> publishCoupon(@PathVariable(name = "couponId") Long couponId,
                                                                                  @AuthenticationPrincipal User user) {
        CouponPublishResponse response = couponPublishService.publishCoupon(couponId, user.getId());
        return ResponseEntity.ok(GlobalApiResponse.success("쿠폰 발급 성공", response));
    }

    @GetMapping("/user/coupon/couponPublish/getCouponPublish")
    @Operation(summary = "유저의 쿠폰 조회")
    public ResponseEntity<GlobalApiResponse<List<CouponPublishResponse>>> getCouponPublish(@AuthenticationPrincipal User user) {
        List<CouponPublishResponse> responses = couponPublishService.getCouponPublish(user);
        return ResponseEntity.ok(GlobalApiResponse.success("쿠폰 조회 성공", responses));
    }
}
