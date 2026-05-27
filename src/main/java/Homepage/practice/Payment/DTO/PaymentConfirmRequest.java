package Homepage.practice.Payment.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentConfirmRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private BigDecimal amount;  // 결제 금액

    /** Mock 테스트용, SUCCESS, FAIL, TIMEOUT 중 하나, 값이 없으면 SUCCESS */
    private String mockResult;  // 응답 결과
}
