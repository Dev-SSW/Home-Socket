package Homepage.practice.Review;

import Homepage.practice.Exception.ItemNotFound;
import Homepage.practice.Exception.ReviewNotFound;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Review.DTO.ReviewRequest;
import Homepage.practice.Review.DTO.ReviewResponse;
import Homepage.practice.Review.DTO.ReviewUpdateRequest;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;

    /** 리뷰 생성 */
    @Transactional
    public ReviewResponse createReview(Long userId, Long itemId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        int reviewCount = (int) reviewRepository.countByItemId(itemId);
        Review review = Review.createReview(user, item, request, reviewCount);
        reviewRepository.save(review);
        return ReviewResponse.fromEntity(review);
    }

    /** 리뷰 수정 */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFound("아이디에 해당하는 리뷰가 없습니다."));
        Item item = itemRepository.findById(review.getItem().getId())
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        int reviewCount = (int) reviewRepository.countByItemId(review.getItem().getId());
        review.updateReview(request, reviewCount);
        return ReviewResponse.fromEntity(review);
    }

    /** 리뷰 삭제 */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFound("아이디에 해당하는 리뷰가 없습니다."));
        reviewRepository.delete(review);
    }

    /** 리뷰 단건 조회 */
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFound("아이디에 해당하는 리뷰가 없습니다."));
        return ReviewResponse.fromEntity(review);
    }

    /** 유저 리뷰 전체 조회 */
    public List<ReviewResponse> getUserReview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 아이템 리뷰 전체 조회 */
    public List<ReviewResponse> getItemReview(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        return reviewRepository.findByItemId(itemId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
