package Homepage.practice.Coupon;

import Homepage.practice.Coupon.DTO.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    /**
     * 쿠폰이 이미 존재하는지 확인
     */
    boolean existsByName(String name);

    /**
     * 쿠폰 이름으로 쿠폰 찾기
     */
    Optional<Coupon> findByName(String name);

    /**
     * 모든 쿠폰 조회
     */
    @Query("select new Homepage.practice.Coupon.DTO.CouponResponse(c.id, c.name, c.discount, c.validStart, c.validEnd, c.afterIssue) from Coupon c")
    Page<CouponResponse> findAllCoupons(Pageable pageable);
    
    /**
     * 특정 쿠폰 조회
     */
    @Query("select new Homepage.practice.Coupon.DTO.CouponResponse(c.id, c.name, c.discount, c.validStart, c.validEnd, c.afterIssue) " +
            "from Coupon c where c.id = :couponId")
    Optional<CouponResponse> findCouponById(@Param("couponId") Long couponId);

}
