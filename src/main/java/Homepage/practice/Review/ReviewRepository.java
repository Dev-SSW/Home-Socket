package Homepage.practice.Review;

import Homepage.practice.Review.DTO.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /** 해당 아이템에 대한 리뷰 갯수 조회 */
    long countByItemId(Long itemId);

    /** 특정 리뷰 조회 */
    @Query("select new Homepage.practice.Review.DTO.ReviewResponse(" +
            "r.id, r.title, r.comment, r.star, r.reviewDate, u.id, u.username, i.id, i.name) " +
            "from Review r join r.user u join r.item i where r.id = :reviewId")
    ReviewResponse findReviewById(@Param("reviewId") Long reviewId);
    
    /** 유저의 리뷰 목록 조회 */
    @Query("select new Homepage.practice.Review.DTO.ReviewResponse(" +
            "r.id, r.title, r.comment, r.star, r.reviewDate, u.id, u.username, i.id, i.name) " +
            "from Review r join r.user u join r.item i where r.user.id = :userId")
    Page<ReviewResponse> findReviewByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /** 아이템의 리뷰 목록 조회 */
    @Query("select new Homepage.practice.Review.DTO.ReviewResponse(" +
            "r.id, r.title, r.comment, r.star, r.reviewDate, u.id, u.username, i.id, i.name) " +
            "from Review r join r.user u join r.item i where r.item.id = :itemId")
    Page<ReviewResponse> findReviewByItemId(@Param("itemId") Long itemId, Pageable pageable);
}
