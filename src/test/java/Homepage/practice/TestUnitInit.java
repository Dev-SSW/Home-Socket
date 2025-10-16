package Homepage.practice;

import Homepage.practice.Cart.Cart;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.Category.Category;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.DTO.CouponRequest;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.Delivery.Address;
import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.Delivery;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.Item;
import Homepage.practice.Order.Order;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestUnitInit {
    private TestUnitInit() {}

    public static User createUser(Long id) {
        User user = User.createUser(
                new SignupRequest("user1", "pass", LocalDate.of(2000, 1, 1), "홍길동"), "{noop}pass");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static Category createCategory(Long id) {
        Category category = Category.createCategory(
                new CategoryRequest("category1", 0, 1, null), null);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    public static Item createItem(Long id, Category category) {
        Item item = Item.createItem(
                category, new ItemRequest("item1", 1000, 10000));
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    public static Coupon createCoupon(Long id) {
        Coupon coupon = Coupon.createCoupon(
                new CouponRequest("coupon1", BigDecimal.valueOf(1000), LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1), 90));
        ReflectionTestUtils.setField(coupon, "id", id);
        return coupon;
    }

    public static CouponPublish createCouponPublish(Long id, Coupon coupon, User user) {
        CouponPublish cp = CouponPublish.createCoupon(coupon, user);
        ReflectionTestUtils.setField(cp, "id", id);
        return cp;
    }

    public static Cart createCart(Long id, User user) {
        Cart cart = Cart.createCart(user);
        ReflectionTestUtils.setField(cart, "id", id);
        return cart;
    }

    public static CartItem createCartItem(Long id, Cart cart, Item item, int quantity) {
        CartItem cartItem = CartItem.createCartItem(cart, item, quantity);
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }

    public static Address createAddress(Long id, User user) {
        Address address = Address.createAddress(
                user, new AddressRequest("address1", "detailStreet1", "zipcode", true));
        ReflectionTestUtils.setField(address, "id", id);
        return address;
    }

    public static Delivery createDelivery(Long id, Order order, Address address) {
        Delivery delivery = Delivery.createDelivery(order, address);
        ReflectionTestUtils.setField(delivery, "id", id);
        return delivery;
    }

    public static Order createOrder(Long id, User user, CouponPublish couponPublish, BigDecimal totalPrice) {
        Order order = Order.createOrder(user, couponPublish, totalPrice);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    public static OrderItem createOrderItem(Long id, Item item, int quantity) {
        OrderItem orderItem = OrderItem.createOrderItem(item, quantity);
        ReflectionTestUtils.setField(orderItem, "id", id);
        return orderItem;
    }
}
