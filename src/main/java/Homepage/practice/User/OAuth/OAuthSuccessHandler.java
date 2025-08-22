package Homepage.practice.User.OAuth;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.JWT.JwtUtils;
import Homepage.practice.User.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            // User로 캐스팅 (jwt 발급을 위함)
            User oAuth2User = (User) authentication.getPrincipal();

            // JWT 토큰 발급
            String accessToken = jwtUtils.generateToken(oAuth2User);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), oAuth2User);

            // RefreshToken을 HttpOnly 쿠키에 저장
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);           // XSS 공격 방지
            cookie.setSecure(true);             // HTTPS에서만 전송
            cookie.setPath("/");                // 전체 경로 허용
            cookie.setMaxAge(7 * 24 * 60 * 60);        // 7일
            response.addCookie(cookie);         // 쿠키에 저장

            // AccessToken만 JSON 응답으로
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("accessToken", accessToken);

            GlobalApiResponse<Map<String, String>> apiResponse = GlobalApiResponse.success("로그인 성공", tokenData);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
        } catch (Exception e) {
            // JWT 발급/쿠키 저장 중 오류 발생 시
            GlobalApiResponse<String> apiResponse = GlobalApiResponse.fail(
                    "토큰 발급 중 오류가 발생했습니다.",
                    "JWT_ISSUE_FAILED"
            );

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
        }
    }
}
