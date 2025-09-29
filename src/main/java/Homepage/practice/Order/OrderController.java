package Homepage.practice.Order;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.Order.DTO.*;
import Homepage.practice.User.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order", description = "주문 관리 API")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/user/order/createOrder")
    @Operation(summary = "개별 바로 주문")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> createOrder(@AuthenticationPrincipal User user,
                                                                        @Valid @RequestBody OrderIndividualRequest request) {
        OrderResponse response = orderService.createOrder(user.getId(), request);
        return ResponseEntity.ok(GlobalApiResponse.success("개별 주문 성공", response));
    }

    @PostMapping("/user/order/createCartOrder")
    @Operation(summary = "장바구니로 주문")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> createCartOrder(@AuthenticationPrincipal User user,
                                                                            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createCartOrder(user.getId(), request);
        return ResponseEntity.ok(GlobalApiResponse.success("장바구니로 주문 성공", response));
    }

    @DeleteMapping("/user/order/{orderId}/cancelOrder")
    @Operation(summary = "주문 취소")
    public ResponseEntity<GlobalApiResponse<?>> cancelOrder(@PathVariable(name = "orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(GlobalApiResponse.success("주문 취소 성공", null));
    }

    @GetMapping("/user/order/getOrderList")
    @Operation(summary = "사용자의 주문 목록 조회")
    public ResponseEntity<GlobalApiResponse<List<OrderListResponse>>> createCartOrder(@AuthenticationPrincipal User user) {
        List<OrderListResponse> responses = orderService.getOrderList(user.getId());
        return ResponseEntity.ok(GlobalApiResponse.success("사용자의 주문 목록 조회 성공", responses));
    }

    @GetMapping("/user/order/{orderId}/getOrderDetail")
    @Operation(summary = "주문 상세 페이지")
    public ResponseEntity<GlobalApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable(name = "orderId") Long orderId) {
        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(GlobalApiResponse.success("주문 상세 페이지 조회 성공", response));
    }

    @GetMapping("/user/order/getOrderPage")
    @Operation(summary = "주문 페이지")
    public ResponseEntity<GlobalApiResponse<OrderPageResponse>> getOrderPage(@AuthenticationPrincipal User user) {
        OrderPageResponse response = orderService.getOrderPage(user.getId());
        return ResponseEntity.ok(GlobalApiResponse.success("주문 페이지 조회 성공", response));
    }
}
