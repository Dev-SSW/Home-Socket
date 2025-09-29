package Homepage.practice.Delivery;

import Homepage.practice.Exception.DeliveryNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;

    /** 배송 상태 변경 */
    @Transactional
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFound("아이디에 해당하는 배송이 없습니다."));
        delivery.setStatus(status);
    }
}
