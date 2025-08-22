package Homepage.practice.User.OAuth;

import Homepage.practice.Exception.GlobalApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorCode = "OAUTH_LOGIN_FAILED";
        String errorMsg = "OAuth2 로그인 실패";

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            // OAuth2 세부 오류 코드 추출
            errorCode = oauthEx.getError().getErrorCode();  // invalid_request, access_denied
            errorMsg = oauthEx.getError().getDescription(); // 제공자에게서 전달받은 메시지
        }

        GlobalApiResponse<String> apiResponse = GlobalApiResponse.fail("OAuth2 로그인 실패: " + errorMsg, errorCode);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}
