package Homepage.practice.Cart;

import Homepage.practice.Cart.DTO.CartResponse;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.CartItem.DTO.CartItemRequest;
import Homepage.practice.CartItem.DTO.CartItemUpdateRequest;
import Homepage.practice.Category.Category;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnitCart {
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Category testCategory;
    private Item testItem;

    @BeforeEach
    void setup() {
        testUser = TestUnitInit.createUser(1L);
        testCart = TestUnitInit.createCart(2L, testUser);
        testCategory = TestUnitInit.createCategory(3L);
        testItem = TestUnitInit.createItem(4L,testCategory);
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 이미 존재")
    void getCart_success1() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));

        // when
        CartResponse response = cartService.getCart(testUser.getUsername());

        // then
        assertThat(response.getId()).isEqualTo(testCart.getId());
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 존재하지 않음")
    void getCart_success2() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.empty());
        // save 시 객체를 생성하기에 지정해줘야 함
        given(cartRepository.save(any(Cart.class))).willReturn(testCart);

        // when
        CartResponse response = cartService.getCart(testUser.getUsername());

        // then
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 성공 - 이미 존재하는 CartItem")
    void addItem_success1() {
        // given
        CartItem testCartItem = TestUnitInit.createCartItem(5L, testCart, testItem, 2);
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));

        // when
        CartResponse response = cartService.addItem(testUser.getUsername(), new CartItemRequest(testItem.getId(), 3));

        // then
        assertThat(response.getCartItemList().get(0).getId()).isEqualTo(testCartItem.getId());
        assertThat(response.getCartItemList().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 성공 - 새로운 CartItem 생성")
    void addItem_success2() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));

        // when
        CartResponse response = cartService.addItem(testUser.getUsername(), new CartItemRequest(testItem.getId(), 2));

        // then
        assertThat(response.getCartItemList().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니 안 아이템의 수량 변경 성공")
    void updateItem_success() {
        // given
        CartItem testCartItem = TestUnitInit.createCartItem(5L, testCart, testItem, 2);
        given(cartItemRepository.findById(testCartItem.getId())).willReturn(Optional.of(testCartItem));

        // when
        CartResponse response = cartService.updateItem(testUser, new CartItemUpdateRequest(testCartItem.getId(), 3));

        // then
        assertThat(response.getCartItemList().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니 안 아이템 삭제하기")
    void deleteItem() {
        // given
        CartItem testCartItem = TestUnitInit.createCartItem(5L, testCart, testItem, 2);
        given(cartItemRepository.findById(testCartItem.getId())).willReturn(Optional.of(testCartItem));

        // when
        CartResponse response = cartService.deleteItem(testUser, testCartItem.getId());

        // then
        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니에서 특정 아이템들만 제거 성공")
    void deleteItems_success() {
        // given
        CartItem testCartItem = TestUnitInit.createCartItem(5L, testCart, testItem, 2);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(cartItemRepository.findById(testCartItem.getId())).willReturn(Optional.of(testCartItem));

        // when
        CartResponse response = cartService.deleteItems(testUser, List.of(testCartItem.getId()));

        // then
        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() {
        // given
        CartItem testCartItem = TestUnitInit.createCartItem(5L, testCart, testItem, 2);
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));

        // when
        CartResponse response = cartService.clearCart(testUser);

        // then
        assertThat(response.getCartItemList()).isEmpty();
    }
}