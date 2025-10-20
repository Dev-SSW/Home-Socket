package Homepage.practice.User;

import Homepage.practice.TestUnitInit;
import Homepage.practice.User.JWT.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


public class UnitJwtUtils {
    // 의존성이 없는 순수 유틸 클래스이므로 실제 객체를 생성하여 테스트
    private JwtUtils jwtUtils;
    private User testUser;

    @BeforeEach
    void setup() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretKey", "testSecretKey12345678testSecretKey123456789testSecretKey123456789");
        jwtUtils.init();
        testUser = TestUnitInit.createUser(1L);
    }

    @Test
    @DisplayName("액세스 토큰 생성")
    void generateToken_test() {
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        assertEquals("user1", jwtUtils.extractUsername(token));
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    @DisplayName("리프레시 토큰 생성")
    void generateRefreshToken_test() {
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), testUser, testUser.getTokenVersion());
        assertEquals("user1", jwtUtils.extractUsername(refreshToken));
        assertFalse(jwtUtils.isTokenExpired(refreshToken));
    }

    @Test
    @DisplayName("회원 ID 추출")
    void extractUsername() {
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        String username = jwtUtils.extractUsername(token);
        assertEquals("user1", username);
    }

    @Test
    @DisplayName("토큰 유효성 검사")
    void tokenValid() {
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        Boolean booleanToken = jwtUtils.isTokenValid(token, testUser);
        assertTrue(booleanToken);
    }

    @Test
    @DisplayName("토큰 기간 만료 검사")
    void tokenExpired() {
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        Boolean booleanToken = jwtUtils.isTokenExpired(token);
        assertFalse(booleanToken);
    }

    @Test
    @DisplayName("만료된 토큰 생성")
    void expiredToken() {
        String expiredToken = jwtUtils.generateExpiredToken(new HashMap<>(), testUser, testUser.getTokenVersion());
        assertThrows(ExpiredJwtException.class, () -> jwtUtils.isTokenExpired(expiredToken));
    }

    @Test
    @DisplayName("토큰 버전 추출")
    void extractTokenVersion() {
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        Integer currentVersion = jwtUtils.extractTokenVersion(token);
        assertEquals(1, currentVersion);
    }
}
