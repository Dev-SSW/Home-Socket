package Homepage.practice.Review;

import Homepage.practice.Common.DTO.PageResponse;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;

    /** 리뷰 생성 */
    @Transactional
    @CacheEvict(
            cacheNames = {"getItemReview", "getReview", "getItem", "getAllItem", "getItemsByCategory"}, // 리뷰로 상품의 평점이 바뀜
            allEntries = true
    )
    public ReviewResponse createReview(Long userId, Long itemId, ReviewRequest request) {
        // 존재 여부만 확인 (불필요한 전체 객체 조회 방지)
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound("아이디에 해당하는 회원이 없습니다.");
        }
        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotFound("아이디에 해당하는 아이템이 없습니다.");
        }

        int reviewCount = (int) reviewRepository.countByItemId(itemId);
        
        // 생성 시에 Proxy 사용
        User user = userRepository.getReferenceById(userId);
        Item item = itemRepository.getReferenceById(itemId);
        
        Review review = Review.createReview(user, item, request, reviewCount + 1);
        reviewRepository.save(review);
        return ReviewResponse.fromEntity(review);
    }

    /** 리뷰 수정 */
    @Transactional
    @CacheEvict(
            cacheNames = {"getItemReview", "getReview", "getItem", "getAllItem", "getItemsByCategory"}, // 리뷰로 상품의 평점이 바뀜
            allEntries = true
    )
    public ReviewResponse updateReview(Long userId,Long reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFound("아이디에 해당하는 리뷰가 없습니다."));

        // 소유자 검증
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotFound("수정할 수 있는 리뷰가 없습니다.");
        }

        if (!itemRepository.existsById(review.getItem().getId())) {
            throw new ItemNotFound("아이디에 해당하는 아이템이 없습니다.");
        }

        int reviewCount = (int) reviewRepository.countByItemId(review.getItem().getId());
        review.updateReview(request, reviewCount);
        return ReviewResponse.fromEntity(review);
    }

    /** 리뷰 삭제 */
    @Transactional
    @CacheEvict(
            cacheNames = {"getItemReview", "getReview", "getItem", "getAllItem", "getItemsByCategory"}, // 리뷰로 상품의 평점이 바뀜
            allEntries = true
    )
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFound("아이디에 해당하는 리뷰가 없습니다."));
        // 소유자 검증
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotFound("수정할 수 있는 리뷰가 없습니다.");
        }

        reviewRepository.delete(review);

        Item item = review.getItem();

        reviewRepository.delete(review);
        reviewRepository.flush();
        // 리뷰 삭제 시 별점 재계산
        double avgStar = reviewRepository.findAverageStarByItemId(item.getId());
        item.changeAvgStar((float) avgStar);
    }

    /** 리뷰 단건 조회 */
    @Cacheable(cacheNames = "getReview", key = "#reviewId")
    public ReviewResponse getReview(Long reviewId) {
        return reviewRepository.findReviewById(reviewId);
    }

    /** 유저 리뷰 전체 조회 */
    // 로그인 사용자별 개인 데이터이므로 public 조회보다 캐시 효율이 낮아 캐시 적용 X
    public Page<ReviewResponse> getUserReview(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound("아이디에 해당하는 회원이 없습니다.");
        }
        return reviewRepository.findReviewByUserId(userId, pageable );
    }

    /** 아이템 리뷰 전체 조회 */
    @Cacheable(
            cacheNames = "getItemReview",
            key = "#itemId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public PageResponse<ReviewResponse> getItemReview(Long itemId, Pageable pageable) {
        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotFound("아이디에 해당하는 아이템이 없습니다.");
        }
        Page<ReviewResponse> page = reviewRepository.findReviewByItemId(itemId, pageable);
        return PageResponse.from(page);
    }
}
