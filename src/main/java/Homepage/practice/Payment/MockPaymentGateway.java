package Homepage.practice.Payment;

import Homepage.practice.Payment.DTO.MockPaymentConfirmRequest;
import Homepage.practice.Payment.DTO.MockPaymentConfirmResponse;
import Homepage.practice.Payment.DTO.PaymentConfirmRequest;
import Homepage.practice.Payment.DTO.PaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class MockPaymentGateway implements PaymentGateway {
    private final RestClient restClient;

    @Value("${payment.mock.base-url}")
    private String baseUrl;

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest command) {
        MockPaymentConfirmResponse response = restClient.post()
                .uri(baseUrl + "/confirm")
                .body(new MockPaymentConfirmRequest(
                        command.getOrderId(),
                        command.getAmount(),
                        command.getMockResult()
                ))
                .retrieve()
                .body(MockPaymentConfirmResponse.class);

        if (response == null) {
            return new PaymentConfirmResponse(false, null, "Mock 결제 응답이 없습니다.");
        }

        return new PaymentConfirmResponse(
                response.isSuccess(),
                response.getPaymentKey(),
                response.getMessage()
        );
    }
}
