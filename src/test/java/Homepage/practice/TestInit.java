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

public class TestInit {
    public static User createUser(UserRepository userRepository) {
        SignupRequest request = new SignupRequest("user1", "pass1", LocalDate.of(2000, 1, 1), "홍길동");
        String encodedPassword = "{noop}pass1";
        User user = User.createUser(request, encodedPassword);
        return userRepository.save(user);
    }

    public static Category createCategory(CategoryRepository categoryRepository) {
        CategoryRequest request = new CategoryRequest("category1", 0, 1, null);
        Category category = Category.createCategory(request, null);
        return categoryRepository.save(category);
    }

    public static Item createItem(ItemRepository itemRepository, Category category) {
        ItemRequest request = new ItemRequest("item1", 1000, 10000);
        Item item = Item.createItem(category, request);
        return itemRepository.save(item);
    }

    public static Coupon createCoupon(CouponRepository couponRepository) {
        CouponRequest request = new CouponRequest("coupon1", BigDecimal.valueOf(500), LocalDate.of(2000,1,1), LocalDate.of(2100,1,1), 30);
        Coupon coupon = Coupon.createCoupon(request);
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
        AddressRequest request = new AddressRequest("street1", "detailStreet1", "zipcode", true);
        Address address = Address.createAddress(user, request);
        return addressRepository.save(address);
    }

    public static Delivery createDelivery(DeliveryRepository deliveryRepository, Order order, Address address) {
        Delivery delivery = Delivery.createDelivery(order, address);
        return deliveryRepository.save(delivery);
    }

    public static Order createOrder(OrderRepository orderRepository, User user, Address address, CouponPublish couponPublish, BigDecimal totalPrice) {
        Order order = Order.createOrder(user, address, couponPublish, totalPrice);
        return orderRepository.save(order);
    }

    public static OrderItem createOrderItem(OrderItemRepository orderItemRepository, Item item, int quantity) {
        OrderItem orderItem = OrderItem.createOrderItem(item, quantity);
        return orderItemRepository.save(orderItem);
    }
}
