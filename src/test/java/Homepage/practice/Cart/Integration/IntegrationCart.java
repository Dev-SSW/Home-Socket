package Homepage.practice.Cart.Integration;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.CartItem.DTO.CartItemRequest;
import Homepage.practice.CartItem.DTO.CartItemUpdateRequest;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.User.Role;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc  // MockMvc 빈 자동 구성
@Transactional
@Rollback
class IntegrationCart {
    // 테스트 인프라
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 시 사용
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    private User testUser;
    private Item testItem;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.builder()
                        .username("user1")
                        .password("pass1")
                        .role(Role.ROLE_USER)
                        .tokenVersion(1)
                        .build()
        );
        testCart = cartRepository.save(Cart.createCart(testUser));
        testItem = itemRepository.save(
                Item.builder()
                        .name("item1")
                        .stock(10)
                        .itemPrice(1000)
                        .avgStar(0)
                        .build()
        );
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_success1() throws Exception {
        mockMvc.perform(get("/user/cart/getCart")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(testCart.getId()));
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 성공")
    void addItem_success1() throws Exception {
        CartItemRequest request = new CartItemRequest(testItem.getId(), 2);
        mockMvc.perform(post("/user/cart/addItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 아이템 추가 성공"))
                .andExpect(jsonPath("$.data.cartItemList[0].itemId").value(testItem.getId()))
                .andExpect(jsonPath("$.data.cartItemList[0].quantity").value(2));
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 실패 - 아이템 없음")
    void addItem_fail() throws Exception {
        CartItemRequest request = new CartItemRequest(99L, 3);
        mockMvc.perform(post("/user/cart/addItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."));
    }

    @Test
    @DisplayName("장바구니 안 아이템의 수량 변경 성공")
    void updateItem_success() throws Exception {
        CartItem cartItem = cartItemRepository.save(CartItem.createCartItem(testCart, testItem, 2));
        CartItemUpdateRequest request = new CartItemUpdateRequest(cartItem.getId(), 5);

        mockMvc.perform(put("/user/cart/updateItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 안 아이템의 수량 변경 성공"))
                .andExpect(jsonPath("$.data.cartItemList[0].quantity").value(5));
    }

    @Test
    @DisplayName("장바구니 안 아이템의 수량 변경 실패 - 해당 장바구니 없음")
    void updateItem_fail1() throws Exception {
        CartItemUpdateRequest request = new CartItemUpdateRequest(999L, 5);

        mockMvc.perform(put("/user/cart/updateItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 장바구니 아이템이 없습니다."));
    }

    @Test
    @DisplayName("장바구니 안 아이템 삭제하기")
    void deleteItem() throws Exception {
        CartItem cartItem = cartItemRepository.save(CartItem.createCartItem(testCart, testItem, 2));

        mockMvc.perform(delete("/user/cart/{cartItemId}/deleteItem", cartItem.getId())
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 안 아이템 삭제하기 성공"));
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() throws Exception {
        cartItemRepository.save(CartItem.createCartItem(testCart, testItem, 2));

        mockMvc.perform(delete("/user/cart/clearCart")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 비우기 성공"))
                .andExpect(jsonPath("$.data.cartItemList").isEmpty());
    }
}