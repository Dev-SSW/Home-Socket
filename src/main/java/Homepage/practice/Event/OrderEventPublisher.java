package Homepage.practice.Event;

import Homepage.practice.Event.DTO.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    // Kafka 이벤트 발행 책임을 담당하는 클래스
    // KafkaTemplate는 Kafka에서 제공하는 메시지 발행 도구
    private final KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;
    @Value("${app.kafka.topics.order-paid}")
    private String orderPaidTopic;

    public void publishOrderPaidAfterCommit(OrderPaidEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {    // true이면 현재 코드가 트랜잭션 안에서 실행 중이라는 의미
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        // 트랜잭션 커밋 후에 실행 (Kafka 이벤트가 먼저 발행되고 DB 트랜잭션이 나중에 rollback되면 문제가 생길 수 있음)
                        // 예외 발생 (Ex. 결제 성공 처리 중 오류 발생) 시 이벤트가 발행되지 않도록 막을 수 있음
                        public void afterCommit() {
                            send(event);
                        }
                    }
            );
            return;
        }
        send(event);
    }

    private void send(OrderPaidEvent event) {
        // 이후에 추가될 다른 이벤트들에서 같은 주문ID에 대한 이벤트는 같은 partition으로 처리되게 하도록 하기 위해 key를 orderId로 사용
        String key = String.valueOf(event.getOrderId());

        // 실제 발행
        kafkaTemplate.send(orderPaidTopic, key, event)
                // Kafka 전송 결과가 나왔을 때 실행되는 callback (성공 시 ex == null, 실패 시 ex != null)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // 실패했다고 rollback 되지 않음 (afterCommit으로 인해 커밋 이후 send()를 보내기 때문)
                        // 단점 : DB 커밋 성공이후 오류로 인해 이벤트만 발행 실패하는 경우가 있음 -> 별도 보완 필요 (Outbox Pattern)
                        log.error("OrderPaidEvent 발행 실패. orderId={}, eventId={}", event.getOrderId(), event.getEventId(), ex);
                        return;
                    }
                    log.info("OrderPaidEvent 발행 성공. topic={}, orderId={}, eventId={}", orderPaidTopic, event.getOrderId(), event.getEventId());
                });
    }
}
