package Homepage.practice.Review;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.Review.DTO.ReviewRequest;
import Homepage.practice.Review.DTO.ReviewResponse;
import Homepage.practice.Review.DTO.ReviewUpdateRequest;
import Homepage.practice.User.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequiredArgsConstructor
public class ReviewController {
    private ReviewService reviewService;

    @PostMapping("/user/item/{itemId}/review/createReview/")
    @Operation(summary = "리뷰 생성하기")
    public ResponseEntity<GlobalApiResponse<ReviewResponse>> createReview(@AuthenticationPrincipal User user,
                                                                          @PathVariable(name = "itemId") Long itemId,
                                                                          @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(user.getId(), itemId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("리뷰 생성 성공", response));
    }

    @PutMapping("/user/item/review/{reviewId}/updateReview/")
    @Operation(summary = "리뷰 수정하기")
    public ResponseEntity<GlobalApiResponse<ReviewResponse>> updateReview(@PathVariable(name = "reviewId") Long reviewId,
                                                                          @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewResponse response = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("리뷰 수정 성공", response));
    }

    @DeleteMapping("/user/item/review/{reviewId}/deleteReview/")
    @Operation(summary = "리뷰 삭제하기")
    public ResponseEntity<GlobalApiResponse<?>> deleteReview(@PathVariable(name = "reviewId") Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(GlobalApiResponse.success("리뷰 삭제 성공", null));
    }

    @GetMapping("/user/item/review/{reviewId}/getReview/")
    @Operation(summary = "리뷰 단건 조회")
    public ResponseEntity<GlobalApiResponse<ReviewResponse>> getReview(@PathVariable(name = "reviewId") Long reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(GlobalApiResponse.success("리뷰 단건 조회 성공", response));
    }

    @GetMapping("/user/getUserReview/")
    @Operation(summary = "유저 리뷰 전체 조회")
    public ResponseEntity<GlobalApiResponse<List<ReviewResponse>>> getUserReview(@AuthenticationPrincipal User user) {
        List<ReviewResponse> responses = reviewService.getUserReview(user.getId());
        return ResponseEntity.ok(GlobalApiResponse.success("유저 리뷰 전체 조회 성공", responses));
    }

    @GetMapping("/user/item/{itemId}/review/getItemReview/")
    @Operation(summary = "아이템 리뷰 전체 조회")
    public ResponseEntity<GlobalApiResponse<List<ReviewResponse>>> getItemReview(@PathVariable(name = "itemId") Long itemId) {
        List<ReviewResponse> responses = reviewService.getItemReview(itemId);
        return ResponseEntity.ok(GlobalApiResponse.success("아이템 리뷰 전체 조회 성공", responses));
    }
}
