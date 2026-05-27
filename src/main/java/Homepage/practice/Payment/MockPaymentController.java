package Homepage.practice.Payment;

import Homepage.practice.Payment.DTO.MockPaymentConfirmRequest;
import Homepage.practice.Payment.DTO.MockPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MockPaymentController {
    @PostMapping("/public/mock-payments/confirm")
    public ResponseEntity<MockPaymentConfirmResponse> confirm(@RequestBody MockPaymentConfirmRequest request) {
        String result = request.getMockResult();

        if ("FAIL".equalsIgnoreCase(result)) {
            return ResponseEntity.ok(MockPaymentConfirmResponse.fail("Mock 결제 승인 실패"));
        }

        if ("TIMEOUT".equalsIgnoreCase(result)) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.ok(MockPaymentConfirmResponse.fail("Mock 결제 처리 중단"));
            }
        }

        String paymentKey = "mock_pay_" + request.getOrderId() + "_" + System.currentTimeMillis();

        return ResponseEntity.ok(MockPaymentConfirmResponse.success(paymentKey));
    }
}
