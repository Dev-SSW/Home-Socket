package Homepage.practice.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    /** 쿠폰이 이미 존재하는지 확인 */
    boolean existsByName(String name);

    /** 쿠폰 이름으로 쿠폰 찾기 */
    Optional<Coupon> findByName(String name);
}
