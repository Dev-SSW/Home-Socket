package Homepage.practice.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthChannelInterceptor  webSocketAuthChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");                       // 서버 -> 클라이언트에게 메시지
        registry.setApplicationDestinationPrefixes("/app");    // 클라이언트 -> 서버에게 메시지 (확장성)
        registry.setUserDestinationPrefix("/user");                 // 특정 사용자에게 메세지
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 endpoint - 프론트에서 ws://localhost:8081/ws 로 연결
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // SockJS fallback endpoint, 프론트에서 http://localhost:8081/ws-sockjs 로 연결.
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}
