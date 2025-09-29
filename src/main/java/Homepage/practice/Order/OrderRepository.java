package Homepage.practice.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /** 유저의 주문 모두 조회 */
    List<Order> findByUserId(Long userId);
}
