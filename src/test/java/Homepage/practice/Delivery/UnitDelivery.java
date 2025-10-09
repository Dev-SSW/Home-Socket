package Homepage.practice.Delivery;

import Homepage.practice.Exception.DeliveryNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitDelivery {
    @Mock
    private DeliveryRepository deliveryRepository;
    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery testDelivery;

    @BeforeEach
    void setup() {
        testDelivery = Delivery.builder().id(1L).status(DeliveryStatus.READY).build();
    }

    @Test
    @DisplayName("배송 상태 변경 성공")
    void updateDeliveryStatus_success() {
        // given
        given(deliveryRepository.findById(testDelivery.getId())).willReturn(Optional.of(testDelivery));

        // when
        deliveryService.updateDeliveryStatus(testDelivery.getId(), DeliveryStatus.SHIPPING);

        // then
        assertThat(testDelivery.getStatus()).isEqualTo(DeliveryStatus.SHIPPING);
    }

    @Test
    @DisplayName("배송 상태 변경 실패 - 아이디에 해당하는 배송이 없습니다")
    void updateDeliveryStatus_fail() {
        // given
        given(deliveryRepository.findById(testDelivery.getId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryService.updateDeliveryStatus(testDelivery.getId(), DeliveryStatus.SHIPPING))
                .isInstanceOf(DeliveryNotFound.class)
                .hasMessage("아이디에 해당하는 배송이 없습니다.");
    }
}
