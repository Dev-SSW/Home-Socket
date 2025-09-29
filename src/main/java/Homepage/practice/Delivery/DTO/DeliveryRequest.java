package Homepage.practice.Delivery.DTO;

import Homepage.practice.Delivery.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryRequest {
    @NotNull(message = "배송 상태를 입력해야 합니다.")
    private DeliveryStatus status;
}
