package Homepage.practice.Payment;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.Payment.DTO.PaymentConfirmRequest;
import Homepage.practice.Payment.DTO.PaymentResponse;
import Homepage.practice.User.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 관련 API")
@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/user/payments/confirm")
    @Operation(summary = "결제 승인")
    public ResponseEntity<GlobalApiResponse<PaymentResponse>> confirmPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentConfirmRequest request) {
        PaymentResponse response = paymentService.confirmPayment(user.getId(), request);

        return ResponseEntity.ok(GlobalApiResponse.success("결제 승인 처리 완료", response));
    }
}
