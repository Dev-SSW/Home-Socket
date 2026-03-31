package Homepage.practice.CouponPublish;

import Homepage.practice.CouponPublish.DTO.CouponPublishResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CouponPublishRepository extends JpaRepository<CouponPublish, Long> {
    /** 기간 만료된 모든 발급 쿠폰 찾기 */
    @Query("select cp from CouponPublish cp where cp.status = :status and cp.validEnd < :date")
    List<CouponPublish> findAllByStatusAndValidEndBefore(@Param("status") CouponPublishStatus status, @Param("date") LocalDate date);

    /** 이미 발급된 쿠폰인지 확인 */
    @Query("select case when count(cp) > 0 then true else false end from CouponPublish cp where cp.user.id = :userId and cp.coupon.id = :couponId")
    boolean existsByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);
    
    /** 유저의 사용 가능한 쿠폰 목록 조회 */
    @Query("select cp from CouponPublish cp join fetch cp.coupon where cp.user.id = :userId and cp.status = :status")
    List<CouponPublish> findAvailableCouponsByUserId(
            @Param("userId") Long userId, @Param("status") CouponPublishStatus status);
    
    /** 유저의 쿠폰 목록을 DTO로 조회 */
    @Query("select new Homepage.practice.CouponPublish.DTO.CouponPublishResponse(" +
            "cp.id, cp.validStart, cp.validEnd, cp.status, c.name, c.discount) " +
            "from CouponPublish cp join cp.coupon c where cp.user.id = :userId")
    List<CouponPublishResponse> findCouponPublishByUserId(@Param("userId") Long userId);
}
