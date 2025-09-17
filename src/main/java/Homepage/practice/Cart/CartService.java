package Homepage.practice.Cart;

import Homepage.practice.Cart.DTO.CartResponse;
import Homepage.practice.CartItem.CartItem;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.CartItem.DTO.CartItemRequest;
import Homepage.practice.CartItem.DTO.CartItemUpdateRequest;
import Homepage.practice.Exception.*;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /** 장바구니 있으면 조회, 없으면 생성 */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.createCart(user)));
    }

    /** 장바구니 조회 */
    public CartResponse getCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        Cart cart = getOrCreateCart(user);
        return CartResponse.fromEntity(cart);
    }

    /** 장바구니에 아이템 추가 */
    @Transactional
    public CartResponse addItem(String username, CartItemRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        Cart cart = getOrCreateCart(user);
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));

        // CartItem이 Cart에 이미 존재하는지 확인하고, 존재한다면 가져옴
        CartItem exit = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().equals(item))    //Cart의 CartItem을 순회하며 각 CartItem의 Item이 위의 Item과 같으면 해당 CarItem을 반환
                .findFirst()
                .orElse(null);

        if (exit != null) { // CartItem이 이미 존재할 때
            exit.addQuantity(request.getQuantity());
        } else {            // CartItem이 존재하지 않을 때, 새로운 CartItem 생성
            CartItem cartItem = CartItem.createCartItem(cart, item, request.getQuantity());
        }
        return CartResponse.fromEntity(cart);
    }

    /** 장바구니 안 아이템의 수량 변경 */
    @Transactional
    public CartResponse updateItem(User user, CartItemUpdateRequest request) {
        CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new CartItemNotFound("아이디에 해당하는 장바구니 아이템이 없습니다."));
        // 소유자 검증
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new CartAccessDenied("본인 장바구니만 수정할 수 있습니다.");
        }
        cartItem.updateQuantity(request.getQuantity());
        return CartResponse.fromEntity(cartItem.getCart());
    }

    /** 장바구니 안 아이템 삭제하기 */
    @Transactional
    public CartResponse deleteItem(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFound("아이디에 해당하는 장바구니 아이템이 없습니다."));
        // 소유자 검증
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new CartAccessDenied("본인 장바구니만 수정할 수 있습니다.");
        }
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        return CartResponse.fromEntity(cart);
    }

    /** 장바구니 비우기 */
    @Transactional
    public CartResponse clearCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFound("아이디에 해당하는 장바구니가 없습니다."));
        cart.getCartItems().clear();
        return CartResponse.fromEntity(cart);
    }
}
