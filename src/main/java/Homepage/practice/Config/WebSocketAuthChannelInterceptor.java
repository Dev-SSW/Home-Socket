package Homepage.practice.Config;

import Homepage.practice.User.JWT.JwtUtils;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    // 클라이언트가 WebSocket/STOMP 메시지를 서버로 보낼 때마다 실행
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 메세지를 다루기 쉽게 감싸기
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 메세지 종류 확인 (CONNECT와 SUBSCRIBE만 처리)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // JWT 인증
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 구독 권한 검사
            validateSubscribe(accessor);
        }

        return message;
    }

    // JWT 인증
    private void authenticate(StompHeaderAccessor accessor) {
        // STOMP header에서 Authorization 꺼내기
        String authorization = getFirstNativeHeader(accessor, "Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AccessDeniedException("WebSocket 연결에 JWT가 필요합니다.");
        }

        String token = authorization.substring(7);

        try {
            String username = jwtUtils.extractUsername(token);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AccessDeniedException("WebSocket 사용자를 찾을 수 없습니다."));

            if (jwtUtils.isTokenExpired(token)) {
                throw new AccessDeniedException("만료된 WebSocket JWT입니다.");
            }

            if (!jwtUtils.isTokenValid(token, user)) {
                throw new AccessDeniedException("유효하지 않은 WebSocket JWT입니다.");
            }

            Integer tokenVersion = jwtUtils.extractTokenVersion(token);

            if (!tokenVersion.equals(user.getTokenVersion())) {
                throw new AccessDeniedException("더 이상 유효하지 않은 WebSocket JWT입니다.");
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            // WebSocket 세션에 사용자 정보 등록
            accessor.setUser(authentication);

        } catch (JwtException e) {
            throw new AccessDeniedException("유효하지 않은 WebSocket JWT입니다.");
        }
    }

    // 구독 권한 검사
    private void validateSubscribe(StompHeaderAccessor accessor) {
        // 인증된 사용자 없이 구독하려 하면 차단 (JWT 인증이 실패한 세션은 차단)
        if (accessor.getUser() == null) {
            throw new AccessDeniedException("인증되지 않은 WebSocket 구독입니다.");
        }

        // 구독 destination 확인
        String destination = accessor.getDestination();

        // 관리자 알림 구독만 개별로 처리 (일반 사용자는 별도의 role 검사 없음)
        if ("/user/queue/admin-notifications".equals(destination)) {
            // accessor.getUser()가 UsernamePasswordAuthenticationToken 타입인지 검사
            // 인증 객체가 가진 권한 목록 중 ROLE_ADMIN이 하나라도 있는지 검사
            boolean isAdmin =
                    accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication &&
                    authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

            if (!isAdmin) {
                throw new AccessDeniedException("관리자 알림을 구독할 권한이 없습니다.");
            }
        }
    }

    // STOMP header에서 "name" 꺼내기
    private String getFirstNativeHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }
}
