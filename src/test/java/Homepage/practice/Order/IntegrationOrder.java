package Homepage.practice.Order;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.Cart.CartService;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Coupon.Coupon;
import Homepage.practice.Coupon.CouponRepository;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.CouponPublish.CouponPublishRepository;
import Homepage.practice.Delivery.*;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Order.DTO.OrderIndividualRequest;
import Homepage.practice.Order.DTO.OrderRequest;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.OrderItem.OrderItemRepository;
import Homepage.practice.TestIntegrationInit;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationOrder {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private CouponPublishRepository couponPublishRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private CartService cartService;

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
        testUser = TestIntegrationInit.createUser(userRepository);
        testAddress = TestIntegrationInit.createAddress(addressRepository, testUser);
        testCategory = TestIntegrationInit.createCategory(categoryRepository);
        testItem = TestIntegrationInit.createItem(itemRepository, testCategory);
        testCoupon = TestIntegrationInit.createCoupon(couponRepository);
        testCouponPublish = TestIntegrationInit.createCouponPublish(couponPublishRepository, testCoupon, testUser);
        testCart = TestIntegrationInit.createCart(cartRepository, testUser);
        testCartItem = TestIntegrationInit.createCartItem(cartItemRepository, testCart, testItem, 2);
    }

    @Test
    @Transactional
    @DisplayName("개별 바로 주문 성공")
    void createOrder_success() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/user/order/createOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderIndividualRequest(testAddress.getId(), testCouponPublish.getId(), testItem.getId(), 2)))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("개별 주문 성공"))
                .andExpect(jsonPath("$.data.totalPrice").value(19000));
    }

    @Test
    @Transactional
    @DisplayName("장바구니로 주문 성공 성공")
    void createCartOrder_success() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/user/order/createCartOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderRequest(testAddress.getId(), testCouponPublish.getId(), List.of(testCartItem.getId()))))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니로 주문 성공"))
                .andExpect(jsonPath("$.data.totalPrice").value(19000));
    }

    @Test
    @Transactional
    @DisplayName("주문 취소 성공")
    void cancelOrder_success() throws Exception {
        // given
        Order testOrder = TestIntegrationInit.createOrder(orderRepository, testUser, testCouponPublish, BigDecimal.valueOf(19000));
        OrderItem testOrderItem = TestIntegrationInit.createOrderItem(orderItemRepository, testItem, 2);
        testOrder.addOrderItem(testOrderItem);
        Delivery testDelivery = TestIntegrationInit.createDelivery(deliveryRepository, testOrder, testAddress);

        // when & then
        mockMvc.perform(delete("/user/order/{orderId}/cancelOrder", testOrder.getId())
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주문 취소 성공"));

        Delivery updateDelivery = deliveryRepository.findById(testDelivery.getId()).orElseThrow();
        assertThat(updateDelivery.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    }

    @Test
    @Transactional
    @DisplayName("사용자의 주문 목록 조회 성공 성공")
    void getOrderList_success() throws Exception {
        // given
        Order testOrder = TestIntegrationInit.createOrder(orderRepository, testUser, testCouponPublish, BigDecimal.valueOf(19000));
        OrderItem testOrderItem = TestIntegrationInit.createOrderItem(orderItemRepository, testItem, 2);
        testOrder.addOrderItem(testOrderItem);
        Delivery testDelivery = TestIntegrationInit.createDelivery(deliveryRepository, testOrder, testAddress);

        // when & then
        mockMvc.perform(get("/user/order/getOrderList")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자의 주문 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].totalPrice").value(19000))
                .andExpect(jsonPath("$.data[0].deliveryStatus").value(DeliveryStatus.READY.name()));
    }

    @Test
    @Transactional
    @DisplayName("주문 상세 페이지 조회 성공")
    void getOrderDetail_success() throws Exception {
        // given
        Order testOrder = TestIntegrationInit.createOrder(orderRepository, testUser, testCouponPublish, BigDecimal.valueOf(19000));
        OrderItem testOrderItem = TestIntegrationInit.createOrderItem(orderItemRepository, testItem, 2);
        testOrder.addOrderItem(testOrderItem);
        Delivery testDelivery = TestIntegrationInit.createDelivery(deliveryRepository, testOrder, testAddress);

        // when & then
        mockMvc.perform(get("/user/order/{orderId}/getOrderDetail", testOrder.getId() )
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주문 상세 페이지 조회 성공"))
                .andExpect(jsonPath("$.data.totalPrice").value(19000))
                .andExpect(jsonPath("$.data.deliveryResponse.status").value(DeliveryStatus.READY.name()));
    }

    @Test
    @Transactional
    @DisplayName("주문 페이지 성공")
    void getOrderPage_success() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/user/order/getOrderPage")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주문 페이지 조회 성공"))
                .andExpect(jsonPath("$.data.addressResponses[0].street").value("address1"))
                .andExpect(jsonPath("$.data.couponPublishResponses[0].couponName").value("coupon1"))
                .andExpect(jsonPath("$.data.cartItemResponses[0].itemName").value("item1"));
    }
}
