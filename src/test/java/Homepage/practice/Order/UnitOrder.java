package Homepage.practice.Order;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.Cart.CartService;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.Category.Category;
import Homepage.practice.Coupon.Coupon;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import Homepage.practice.Delivery.*;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Order.DTO.*;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.TestUnitInit;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitOrder {
    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private CouponPublishRepository couponPublishRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartService cartService;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @InjectMocks private OrderService orderService;

    private User testUser;
    private Address testAddress;
    private Category testCategory;
    private Item testItem;
    private Coupon testCoupon;
    private CouponPublish testCouponPublish;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setup() {
        testUser = TestUnitInit.createUser(1L);
        testAddress = TestUnitInit.createAddress(2L, testUser);
        testCategory = TestUnitInit.createCategory(3L);
        testItem = TestUnitInit.createItem(4L, testCategory);
        testCoupon = TestUnitInit.createCoupon(5L);
        testCouponPublish = TestUnitInit.createCouponPublish(6L, testCoupon, testUser);
        testCart = TestUnitInit.createCart(7L, testUser);
        testCartItem = TestUnitInit.createCartItem(8L, testCart, testItem, 2);
    }

    @Test
    @DisplayName("개별 바로 주문 성공")
    void createOrder_success() {
        // given
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.save(any(CartItem.class))).willAnswer(inv -> {
                    CartItem ci = inv.getArgument(0);
                    ReflectionTestUtils.setField(ci, "id", 100L);
                    return ci;
        });
        given(addressRepository.findById(testAddress.getId())).willReturn(Optional.of(testAddress));
        given(couponPublishRepository.findById(testCouponPublish.getId())).willReturn(Optional.of(testCouponPublish));

        // when
        OrderResponse response = orderService.createOrder(testUser.getId(),
                new OrderIndividualRequest(testAddress.getId(), testCouponPublish.getId(), testItem.getId(), 2));

        // then
        verify(cartItemRepository).save(any(CartItem.class));
        verify(deliveryRepository).save(any(Delivery.class));
        verify(orderRepository).save(any(Order.class));
        verify(cartService).deleteItems(testUser, List.of(100L));
        // 총 금액 계산
        assertThat(response.getTotalPrice().compareTo(BigDecimal.valueOf(19000))).isZero();
        // createOrderItem에서 재고 사용
        assertThat(testItem.getStock()).isEqualTo(998);
        // createOrder에서 useCoupon() 사용
        assertThat(testCouponPublish.getStatus()).isEqualTo(CouponPublishStatus.USED);
        // createDelivery에서 READY로 상태 변화
        assertThat(testUser.getOrders().get(0).getDelivery().getStatus()).isEqualTo(DeliveryStatus.READY);
    }

    @Test
    @DisplayName("장바구니로 주문 성공")
    void createCartOrder_success() {
        // given
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(addressRepository.findById(testAddress.getId())).willReturn(Optional.of(testAddress));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(couponPublishRepository.findById(testCouponPublish.getId())).willReturn(Optional.of(testCouponPublish));

        // when
        OrderResponse response = orderService.createCartOrder(testUser.getId(),
                new OrderRequest(testAddress.getId(), testCouponPublish.getId(), List.of(testCartItem.getId())));

        // then
        verify(deliveryRepository).save(any(Delivery.class));
        verify(orderRepository).save(any(Order.class));
        verify(cartService).deleteItems(testUser, List.of(testCartItem.getId()));
        // 총 금액 계산
        assertThat(response.getTotalPrice().compareTo(BigDecimal.valueOf(19000))).isZero();
        // createOrderItem에서 재고 사용
        assertThat(testItem.getStock()).isEqualTo(998);
        // createOrder에서 useCoupon() 사용
        assertThat(testCouponPublish.getStatus()).isEqualTo(CouponPublishStatus.USED);
        // createDelivery에서 READY로 상태 변화
        assertThat(testUser.getOrders().get(0).getDelivery().getStatus()).isEqualTo(DeliveryStatus.READY);
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_success() {
        // given
        Order testOrder = TestUnitInit.createOrder(9L, testUser, testCouponPublish, BigDecimal.valueOf(19000));
        OrderItem testOrderItem = TestUnitInit.createOrderItem(10L, testItem, 2);
        testOrder.addOrderItem(testOrderItem);
        Delivery testDelivery = TestUnitInit.createDelivery(11L, testOrder, testAddress);
        given(orderRepository.findById(testOrder.getId())).willReturn(Optional.of(testOrder));

        // when
        orderService.cancelOrder(testOrder.getId());

        // then
        // order.cancel()에서 Delivery 상태 CANCELLED로 상태 변화
        assertThat(testDelivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
        // order.cancel()에서 orderItems.forEach(OrderItem::cancel)로 item.addStock 수행
        assertThat(testItem.getStock()).isEqualTo(1000);
        // order.cancel()에서 couponPublish.cancelCoupon()으로 CouponPublish 상태 AVAILABLE로 변화
        assertThat(testCouponPublish.getStatus()).isEqualTo(CouponPublishStatus.AVAILABLE);
    }
}
