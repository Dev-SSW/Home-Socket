package Homepage.practice.Order;

import Homepage.practice.Order.DTO.OrderListResponse;
import Homepage.practice.OrderItem.OrderItem;
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

    /** 주문 기본 정보 조회 (배송지 포함) */
    @EntityGraph(attributePaths = {"delivery", "delivery.address"})
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findOrderWithDeliveryById(@Param("orderId") Long orderId);
}
