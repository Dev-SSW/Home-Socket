package Homepage.practice.Order;

import Homepage.practice.Order.DTO.OrderListResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select distinct o
        from Order o
        join fetch o.delivery d
        join fetch o.orderItems oi
        join fetch oi.item i
        left join fetch o.couponPublish cp
        where o.id = :orderId and o.user.id = :userId
        """)
    Optional<Order> findOrderForCancel(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );
}
