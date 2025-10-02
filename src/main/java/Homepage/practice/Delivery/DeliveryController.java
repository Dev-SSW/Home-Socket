package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.DeliveryRequest;
import Homepage.practice.Exception.GlobalApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Delivery", description = "배송 관리 API")
@RestController
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PutMapping("/admin/delivery/{deliveryId}/updateDeliveryStatus")
    @Operation(summary = "배송 상태 변경", description = "배송 상태에는 READY, SHIPPING, COMPLETE, CANCELLED 존재합니다.")
    public ResponseEntity<GlobalApiResponse<?>> updateDeliveryStatus(@PathVariable(name = "deliveryId") Long deliveryId,
                                                                     @Valid @RequestBody DeliveryRequest request) {
        deliveryService.updateDeliveryStatus(deliveryId, request.getStatus());
        return ResponseEntity.ok(GlobalApiResponse.success("배송 상태 변경 성공", null));
    }
}
