package Homepage.practice.Delivery;

import Homepage.practice.Order.Order;
import Homepage.practice.TestUnitInit;
import Homepage.practice.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitDelivery {
    @Mock private DeliveryRepository deliveryRepository;
    @InjectMocks private DeliveryService deliveryService;

    private Delivery testDelivery;

    @BeforeEach
    void setup() {
        User testUser = TestUnitInit.createUser(1L);
        Order testOrder = TestUnitInit.createOrder(2L, testUser, null, BigDecimal.valueOf(10000));
        Address testAddress = TestUnitInit.createAddress(3L, testUser);
        testDelivery = TestUnitInit.createDelivery(4L, testOrder, testAddress);
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
}
