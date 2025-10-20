package Homepage.practice.Cart;

import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.CartItem.DTO.CartItemListRequest;
import Homepage.practice.CartItem.DTO.CartItemRequest;
import Homepage.practice.CartItem.DTO.CartItemUpdateRequest;
import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
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


import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class IntegrationCart {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;

    private User testUser;
    private Category testCategory;
    private Item testItem;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = TestIntegrationInit.createUser(userRepository);
        testCart = TestIntegrationInit.createCart(cartRepository, testUser);
        testCategory = TestIntegrationInit.createCategory(categoryRepository);
        testItem = TestIntegrationInit.createItem(itemRepository, testCategory);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 조회 성공")
    void getCart_success1() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/user/cart/getCart")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(testCart.getId()))
                .andExpect(jsonPath("$.data.totalPrice").value(0));
    }

    @Test
    @Transactional
    @DisplayName("장바구니에 아이템 추가 성공")
    void addItem_success1() throws Exception {
        // given
        CartItem testCartItem = TestIntegrationInit.createCartItem(cartItemRepository, testCart, testItem, 2);

        // when & then
        mockMvc.perform(post("/user/cart/addItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartItemRequest(testItem.getId(), 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 아이템 추가 성공"))
                .andExpect(jsonPath("$.data.cartItemList[0].itemId").value(testItem.getId()))
                .andExpect(jsonPath("$.data.cartItemList[0].quantity").value(5))
                .andExpect(jsonPath("$.data.totalPrice").value(50000));
    }

    @Test
    @Transactional
    @DisplayName("장바구니에 아이템 추가 실패 - 아이템 없음")
    void addItem_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/user/cart/addItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartItemRequest(999L, 3))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."));
    }

    @Test
    @Transactional
    @DisplayName("장바구니 안 아이템의 수량 변경 성공")
    void updateItem_success() throws Exception {
        // given
        CartItem testCartItem = TestIntegrationInit.createCartItem(cartItemRepository, testCart, testItem, 2);

        // when & then
        mockMvc.perform(put("/user/cart/updateItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartItemUpdateRequest(testCartItem.getId(), 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 안 아이템의 수량 변경 성공"))
                .andExpect(jsonPath("$.data.cartItemList[0].quantity").value(5));
    }

    @Test
    @Transactional
    @DisplayName("장바구니 안 아이템의 수량 변경 실패 - 해당 장바구니 없음")
    void updateItem_fail1() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/user/cart/updateItem")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartItemUpdateRequest(999L, 5))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 장바구니 아이템이 없습니다."));
    }

    @Test
    @Transactional
    @DisplayName("장바구니 안 아이템 삭제하기")
    void deleteItem() throws Exception {
        // given
        CartItem testCartItem = TestIntegrationInit.createCartItem(cartItemRepository, testCart, testItem, 2);

        // when & then
        mockMvc.perform(delete("/user/cart/{cartItemId}/deleteItem", testCartItem.getId())
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 안 아이템 삭제하기 성공"));
    }

    @Test
    @Transactional
    @DisplayName("장바구니에서 특정 아이템들만 제거")
    void deleteItems() throws Exception {
        // given
        CartItem testCartItem = TestIntegrationInit.createCartItem(cartItemRepository, testCart, testItem, 2);

        // when & then
        mockMvc.perform(delete("/user/cart/deleteItems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CartItemListRequest(List.of(testCartItem.getId()))))
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니에서 특정 아이템들만 제거 성공"));
    }

    @Test
    @Transactional
    @DisplayName("장바구니 비우기")
    void clearCart() throws Exception {
        // given
        cartItemRepository.save(CartItem.createCartItem(testCart, testItem, 2));

        // when & then
        mockMvc.perform(delete("/user/cart/clearCart")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장바구니 비우기 성공"))
                .andExpect(jsonPath("$.data.cartItemList").isEmpty());
    }
}