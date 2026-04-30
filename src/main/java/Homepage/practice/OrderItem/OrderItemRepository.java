package Homepage.practice.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** 주문 상품 목록 조회 */
    @Query("select oi from OrderItem oi join fetch oi.item i where oi.order.id = :orderId")
    List<OrderItem> findWithItemByOrderId(@Param("orderId") Long orderId);
}
