package Homepage.practice.Coupon;

import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.Coupon.DTO.CouponResponse;
import Homepage.practice.Coupon.DTO.CouponUpdateRequest;
import Homepage.practice.Exception.GlobalApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupon", description = "쿠폰 관리 API")
@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    
    @PostMapping("/admin/coupon/createCoupon")
    @Operation(summary = "쿠폰 생성하기")
    public ResponseEntity<GlobalApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(GlobalApiResponse.success("쿠폰 생성 성공", response));
    }

    @GetMapping("/admin/coupon/getAllCoupon")
    @Operation(summary = "모든 쿠폰 조회하기")
    public ResponseEntity<GlobalApiResponse<List<CouponResponse>>> getAllCoupon(@Valid @RequestBody CouponRequest request) {
        List<CouponResponse> responses = couponService.getAllCoupon();
        return ResponseEntity.ok(GlobalApiResponse.success("모든 쿠폰 조회 성공", responses));
    }

    @GetMapping("/admin/coupon/{couponId}/getCoupon")
    @Operation(summary = "특정 쿠폰 조회하기")
    public ResponseEntity<GlobalApiResponse<CouponResponse>> getCoupon(@PathVariable(name = "couponId") Long couponId) {
        CouponResponse response = couponService.getCoupon(couponId);
        return ResponseEntity.ok(GlobalApiResponse.success("특정 쿠폰 조회 성공", response));
    }

    @PutMapping("/admin/coupon/{couponId}/updateCoupon")
    @Operation(summary = "쿠폰 수정하기")
    public ResponseEntity<GlobalApiResponse<CouponResponse>> updateCoupon(@PathVariable(name = "couponId") Long couponId,
                                                                          @Valid @RequestBody CouponUpdateRequest request) {
        CouponResponse response = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("쿠폰 수정 성공", response));
    }

    @DeleteMapping("/admin/coupon/{couponId}/deleteCoupon")
    @Operation(summary = "쿠폰 삭제하기")
    public ResponseEntity<GlobalApiResponse<CouponResponse>> deleteCoupon(@PathVariable(name = "couponId") Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.ok(GlobalApiResponse.success("쿠폰 삭제 성공", null));
    }
}
