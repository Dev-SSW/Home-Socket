package Homepage.practice.Review;

import Homepage.practice.Item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /** 해당 아이템에 대한 리뷰 갯수 조회 */
    long countByItemId(Long itemId);

    /** 유저의 전체 리뷰 조회 */
    List<Review> findByUserId(Long userId);

    /** 아이템의 전체 리뷰 조회 */
    List<Review> findByItemId(Long itemId);
}
