package Homepage.practice.Order;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.Cart.CartService;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.Delivery.Address;
import Homepage.practice.Delivery.AddressRepository;
import Homepage.practice.Delivery.Delivery;
import Homepage.practice.Delivery.DeliveryRepository;
import Homepage.practice.Exception.*;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Order.DTO.*;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ItemRepository itemRepository;
    private final CouponPublishRepository couponPublishRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final CartService cartService;

    /** 개별 바로 주문 */
    @Transactional
    public OrderResponse createOrder(Long userId, OrderIndividualRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));

        // Cart 가져오기 (없으면 생성)
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.createCart(user)));
        // 장바구니에 바로 구매할 아이템 추가
        CartItem cartItem = CartItem.createCartItem(cart, item, request.getQuantity());
        cartItemRepository.save(cartItem);

        return createCartOrder(userId, new OrderRequest(request.getAddressId(), request.getCouponPublishId(), List.of(cartItem.getId())));
    }

    /** 장바구니로 주문 */
    @Transactional
    public OrderResponse createCartOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AddressNotFound("아이디에 해당하는 주소를 찾을 수 없습니다."));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFound("아이디에 해당하는 장바구니가 없습니다."));

        List<OrderItem> orderItems = cart.getCartItems().stream()
                // 선택된 Item만 가져오기
                .filter(cartItem -> request.getCartItemIds().contains(cartItem.getId()))
                .map(cartItem -> {
                    // cartItem을 순회하며 OrderItem 만들기
                    return OrderItem.createOrderItem(cartItem.getItem(), cartItem.getQuantity());
                })
                .collect(Collectors.toList());

        // 총 합계 구하기
        BigDecimal totalPrice = BigDecimal.valueOf(orderItems.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum());

        CouponPublish couponPublish = null;
        if (request.getCouponPublishId() != null) {
            couponPublish = couponPublishRepository.findById(request.getCouponPublishId())
                    .orElseThrow(() -> new CouponPublishNotFound("아이디에 해당하는 발급 쿠폰이 없습니다."));
            totalPrice = totalPrice.subtract(couponPublish.getCoupon().getDiscount());
        }

        Order order = Order.createOrder(user, couponPublish, totalPrice);
        Delivery delivery = Delivery.createDelivery(order, address);
        deliveryRepository.save(delivery);
        orderItems.forEach(order::addOrderItem);
        orderRepository.save(order);

        cartService.deleteItems(user, request.getCartItemIds());

        return OrderResponse.fromEntity(order);
    }

    /** 주문 취소 */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound("아이디에 해당하는 주문이 없습니다."));
        order.cancel();
    }

    /** 사용자의 주문 목록 조회 */
    public List<OrderListResponse> getOrderList(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 주문 상세 페이지 */
    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound("아이디에 해당하는 주문이 없습니다."));
        return OrderDetailResponse.fromEntity(order);
    }

    /** 주문 페이지 */
    public OrderPageResponse getOrderPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        return OrderPageResponse.of(user);
    }
}
