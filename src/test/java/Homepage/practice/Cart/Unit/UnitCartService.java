package Homepage.practice.Cart.Unit;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.Cart.CartService;
import Homepage.practice.Cart.DTO.CartResponse;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.CartItem.DTO.CartItemRequest;
import Homepage.practice.CartItem.DTO.CartItemUpdateRequest;
import Homepage.practice.Exception.CartAccessDenied;
import Homepage.practice.Exception.CartItemNotFound;
import Homepage.practice.Exception.ItemNotFound;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.User.Role;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnitCartService {
    // 외부 의존성 객체
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    // 테스트 할 클래스
    @InjectMocks
    private CartService cartService;

    // 테스트 필드
    private User testUser;
    private Cart testCart;
    private Item testItem;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .password("pass1")
                .birth(LocalDate.of(2000, 1, 1))
                .name("홍길동")
                .role(Role.ROLE_USER)
                .tokenVersion(1)
                .build();
        testCart = Cart.createCart(testUser);
        testItem = Item.builder()
                .id(1L)
                .name("item1")
                .itemPrice(1000)
                .stock(10)
                .avgStar(0)
                .build();
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 이미 존재")
    void getCart_success1() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        // when
        CartResponse response = cartService.getCart("user1");
        // then
        assertThat(response.getId()).isEqualTo(testCart.getId());
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니 존재하지 않음")
    void getCart_success2() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(testCart);
        // when
        CartResponse response = cartService.getCart("user1");
        // then
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 조회 실패 - 유저 없음")
    void getCart_fail() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> cartService.getCart("user1"))
                .isInstanceOf(UserNotFound.class)
                .hasMessage("아이디에 해당하는 회원이 없습니다.");
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 성공 - 이미 존재하는 CartItem")
    void addItem_success1() {
        CartItemRequest request = new CartItemRequest(1L, 3);
        // 장바구니에 아이템 하나 넣어두기
        CartItem testCartItem = CartItem.createCartItem(testCart, testItem, 2);
        ReflectionTestUtils.setField(testCartItem, "id", 10L); // PK 강제 세팅
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));
        // when
        CartResponse response = cartService.addItem("user1", request);
        // then
        assertThat(response.getCartItemList()).hasSize(1);
        assertThat(response.getCartItemList().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 성공 - 새로운 CartItem 생성")
    void addItem_success2() {
        // given
        CartItemRequest request = new CartItemRequest(testItem.getId(), 2);
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));
        // when
        CartResponse response = cartService.addItem("user1", request);
        // then
        assertThat(response.getCartItemList()).hasSize(1);
        assertThat(response.getCartItemList().get(0).getItemId()).isEqualTo(testItem.getId());
        assertThat(response.getCartItemList().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니에 아이템 추가 실패 - 아이템 없음")
    void addItem_fail() {
        // given
        CartItemRequest request = new CartItemRequest(testItem.getId(), 2);
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> cartService.addItem("user1", request))
                .isInstanceOf(ItemNotFound.class)
                .hasMessage("아이디에 해당하는 아이템이 없습니다.");
    }

    @Test
    @DisplayName("장바구니 안 아이템의 수량 변경 성공")
    void updateItem_success() {
        // given
        // 장바구니에 아이템 하나 넣어두기
        CartItem testCartItem = CartItem.createCartItem(testCart, testItem, 2);
        ReflectionTestUtils.setField(testCartItem, "id", 10L); // PK 강제 세팅
        CartItemUpdateRequest request = new CartItemUpdateRequest(testItem.getId(), 3);
        given(cartItemRepository.findById(1L)).willReturn(Optional.of(testCartItem));
        // when
        CartResponse response = cartService.updateItem(testUser, request);
        // then
        assertThat(response.getCartItemList().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니 안 아이템의 수량 변경 실패 - 해당 장바구니 없음")
    void updateItem_fail1() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest(testItem.getId(), 3);
        given(cartItemRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> cartService.updateItem(testUser, request))
                .isInstanceOf(CartItemNotFound.class)
                .hasMessage("아이디에 해당하는 장바구니 아이템이 없습니다.");
    }

    @Test
    @DisplayName("장바구니 안 아이템의 수량 변경 실패 - 소유자 다름")
    void updateItem_fail2() {
        // given
        // 장바구니에 아이템 하나 넣어두기
        CartItem testCartItem = CartItem.createCartItem(testCart, testItem, 2);
        ReflectionTestUtils.setField(testCartItem, "id", 10L); // PK 강제 세팅
        CartItemUpdateRequest request = new CartItemUpdateRequest(testItem.getId(), 3);
        User failUser = User.builder().id(2L).build();
        given(cartItemRepository.findById(1L)).willReturn(Optional.of(testCartItem));
        // when & then
        assertThatThrownBy(() -> cartService.updateItem(failUser, request))
                .isInstanceOf(CartAccessDenied.class)
                .hasMessage("본인 장바구니만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("장바구니 안 아이템 삭제하기")
    void deleteItem() {
        // given
        // 장바구니에 아이템 하나 넣어두기
        CartItem testCartItem = CartItem.createCartItem(testCart, testItem, 2);
        ReflectionTestUtils.setField(testCartItem, "id", 10L); // PK 강제 세팅
        given(cartItemRepository.findById(1L)).willReturn(Optional.of(testCartItem));
        // when
        cartService.deleteItem(testUser, 1L);
        // then
        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() {
        // given
        // 장바구니에 아이템 하나 넣어두기
        CartItem testCartItem = CartItem.createCartItem(testCart, testItem, 2);
        ReflectionTestUtils.setField(testCartItem, "id", 10L); // PK 강제 세팅
        given(cartRepository.findByUser(testUser)).willReturn(Optional.of(testCart));
        // when
        CartResponse response = cartService.clearCart(testUser);
        // then
        assertThat(response.getCartItemList()).isEmpty();
    }
}