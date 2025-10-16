package Homepage.practice;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.Delivery.Address;
import Homepage.practice.Delivery.AddressRepository;
import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.Delivery;
import Homepage.practice.Delivery.DeliveryRepository;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Order.Order;
import Homepage.practice.Order.OrderRepository;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.OrderItem.OrderItemRepository;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestIntegrationInit {
    private TestIntegrationInit() {}

    public static User createUser(UserRepository userRepository) {
        User user = User.createUser(
                new SignupRequest("user1", "pass1", LocalDate.of(2000, 1, 1), "홍길동"), "{noop}pass1");
        return userRepository.save(user);
    }

    public static Category createCategory(CategoryRepository categoryRepository) {
        Category category = Category.createCategory(
                new CategoryRequest("category1", 0, 1, null), null);
        return categoryRepository.save(category);
    }

    public static Item createItem(ItemRepository itemRepository, Category category) {
        Item item = Item.createItem(
                category, new ItemRequest("item1", 1000, 10000));
        return itemRepository.save(item);
    }

    public static Coupon createCoupon(CouponRepository couponRepository) {
        Coupon coupon = Coupon.createCoupon(
                new CouponRequest("coupon1", BigDecimal.valueOf(1000), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 90));
        return couponRepository.save(coupon);
    }

    public static CouponPublish createCouponPublish(CouponPublishRepository couponPublishRepository, Coupon coupon, User user) {
        CouponPublish couponPublish = CouponPublish.createCoupon(coupon, user);
        return couponPublishRepository.save(couponPublish);
    }

    public static Cart createCart(CartRepository cartRepository, User user) {
        Cart cart = Cart.createCart(user);
        return cartRepository.save(cart);
    }

    public static CartItem createCartItem(CartItemRepository cartItemRepository, Cart cart, Item item, int quantity) {
        CartItem cartItem = CartItem.createCartItem(cart, item, quantity);
        return  cartItemRepository.save(cartItem);
    }

    public static Address createAddress(AddressRepository addressRepository, User user) {
        Address address = Address.createAddress(
                user, new AddressRequest("address1", "detailStreet1", "zipcode", true));
        return addressRepository.save(address);
    }

    public static Delivery createDelivery(DeliveryRepository deliveryRepository, Order order, Address address) {
        Delivery delivery = Delivery.createDelivery(order, address);
        return deliveryRepository.save(delivery);
    }

    public static Order createOrder(OrderRepository orderRepository, User user, CouponPublish couponPublish, BigDecimal totalPrice) {
        Order order = Order.createOrder(user, couponPublish, totalPrice);
        return orderRepository.save(order);
    }

    public static OrderItem createOrderItem(OrderItemRepository orderItemRepository, Item item, int quantity) {
        OrderItem orderItem = OrderItem.createOrderItem(item, quantity);
        return orderItemRepository.save(orderItem);
    }
}
