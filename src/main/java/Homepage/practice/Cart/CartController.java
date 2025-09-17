package Homepage.practice.Cart;

import Homepage.practice.Cart.DTO.CartResponse;
import Homepage.practice.CartItem.DTO.CartItemRequest;
import Homepage.practice.CartItem.DTO.CartItemUpdateRequest;
import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "장바구니 관리 API")
@RestController
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/user/cart/getCart")
    @Operation(summary = "장바구니 조회")
    public ResponseEntity<GlobalApiResponse<CartResponse>> getCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.getCart(user.getUsername());
        return ResponseEntity.ok(GlobalApiResponse.success("장바구니 조회 성공", response));
    }

    @PostMapping("/user/cart/addItem")
    @Operation(summary = "장바구니에 아이템 추가")
    public ResponseEntity<GlobalApiResponse<CartResponse>> addItem(@AuthenticationPrincipal User user,
                                                                   @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(user.getUsername(), request);
        return ResponseEntity.ok(GlobalApiResponse.success("장바구니 아이템 추가 성공", response));
    }

    @GetMapping("/user/cart/updateItem")
    @Operation(summary = "장바구니 안 아이템의 수량 변경")
    public ResponseEntity<GlobalApiResponse<CartResponse>> updateItem(@AuthenticationPrincipal User user,
                                                                      @Valid @RequestBody CartItemUpdateRequest request) {
        CartResponse response = cartService.updateItem(user,request);
        return ResponseEntity.ok(GlobalApiResponse.success("장바구니 안 아이템의 수량 변경 성공", response));
    }

    @GetMapping("/user/cart/{cartItemId}/deleteItem")
    @Operation(summary = "장바구니 안 아이템 삭제하기")
    public ResponseEntity<GlobalApiResponse<CartResponse>> deleteItem(@AuthenticationPrincipal User user,
                                                                      @PathVariable(name = "cartItemId") Long cartItemId) {
        CartResponse response = cartService.deleteItem(user, cartItemId);
        return ResponseEntity.ok(GlobalApiResponse.success("장바구니 안 아이템 삭제하기 성공", response));
    }

    @GetMapping("/user/cart/clearCart")
    @Operation(summary = "장바구니 비우기")
    public ResponseEntity<GlobalApiResponse<CartResponse>> clearCart(@AuthenticationPrincipal User user) {
        CartResponse response = cartService.clearCart(user);
        return ResponseEntity.ok(GlobalApiResponse.success("장바구니 비우기 성공", response));
    }
}
