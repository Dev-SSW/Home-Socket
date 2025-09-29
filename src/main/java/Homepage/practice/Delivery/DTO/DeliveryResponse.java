package Homepage.practice.Delivery.DTO;

import Homepage.practice.Delivery.Delivery;
import Homepage.practice.Delivery.DeliveryStatus;
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
public class DeliveryResponse {
    private Long id;
    private AddressResponse addressResponse;
    private DeliveryStatus status;

    public static DeliveryResponse fromEntity(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .addressResponse(AddressResponse.fromEntity(delivery.getAddress()))
                .status(delivery.getStatus())
                .build();
    }
}
