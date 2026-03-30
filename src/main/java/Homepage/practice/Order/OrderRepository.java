package Homepage.practice.Order;

import Homepage.practice.Order.DTO.OrderListResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /** 사용자의 주문 목록 조회 */
    @Query("select new Homepage.practice.Order.DTO.OrderListResponse(o.id, o.orderDate, o.totalPrice, d.status) " +
            "from Order o join o.delivery d where o.user.id = :userId")
    List<OrderListResponse> findOrderListByUserId(@Param("userId") Long userId);

    /** 상세 주문 내역 조회 */
    @EntityGraph(attributePaths = {"delivery", "delivery.address", "orderItems", "orderItems.item"})
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findOrderDetailById(@Param("orderId") Long orderId);
}
