package Homepage.practice.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    // 결제 완료된 주문 이벤트들이 들어가는 topic
    @Bean
    public NewTopic orderPaidTopic(@Value("${app.kafka.topics.order-paid}") String topicName) {
        return TopicBuilder.name(topicName)
                // topic을 3개 파티션으로 나눔
                .partitions(3)
                // 로컬 Kafka 단일 broker 1개 (Partition의 복제본 개수)
                .replicas(1)
                .build();
    }
}
