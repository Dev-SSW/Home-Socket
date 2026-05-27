package Homepage.practice.Payment.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockPaymentConfirmResponse {
    private boolean success;
    private String paymentKey;
    private String message;

    public static MockPaymentConfirmResponse success(String paymentKey) {
        return new MockPaymentConfirmResponse(true, paymentKey, "결제 승인 성공");
    }

    public static MockPaymentConfirmResponse fail(String message) {
        return new MockPaymentConfirmResponse(false, null, message);
    }
}
