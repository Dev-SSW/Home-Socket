package Homepage.practice.User.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JwtUtils {
    private SecretKey key;
    private static final long EXPIRATION_TIME = 86400000;           // 24 hours
    private static final long REFRESH_EXPIRATION_TIME = 604800000;  // 7 days

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** JWT 토큰 생성 */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())                                  // "sub" 클레임에 주입
                .setIssuedAt(new Date(System.currentTimeMillis()))                      // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)                                // HmacSHA256 키로 서명
                .compact();
    }

    /** JWT 리프레쉬 토큰 생성 */
    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)                                                      // 추가 클레임 저장 가능
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 회원 ID 추출 */
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /** 넘겨받은 클레임 정보 추출 */
    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    /** JWT 토큰 유효성 검사 (유효하면 True) */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername());
    }

    /** JWT 토큰 기간 만료 검사 (토큰이 만료되었으면 True) */
    public boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    /** 테스트를 위한 만료된 토큰 생성 */
    public String generateExpiredToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)                                                      // 추가 클레임 저장 가능
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
