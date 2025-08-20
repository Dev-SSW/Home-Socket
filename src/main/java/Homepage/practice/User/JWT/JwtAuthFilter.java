package Homepage.practice.User.JWT;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 예외 발생 시 응답 작성 */
    private void write(HttpServletResponse response, int status, GlobalApiResponse<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

    /** 명시적으로 필터를 거치지 않아도 될 URL을 지정 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        System.out.println("필터를 거치지 않습니다.");
        String URL = request.getRequestURI();
        String[] excludePath = {
                "/public", "/error", "/swagger-ui", "/v3/api-docs", "/v3/api-docs.yaml", "/v3/api-docs/swagger-config"
        };
        return  Arrays.stream(excludePath).anyMatch(URL::startsWith);
    }

    /** 필터 설정 부분 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new JwtException("토큰이 존재하지 않습니다.");
            }

            String jwtToken = authHeader.substring(7);
            String username = jwtUtils.extractUsername(jwtToken);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtils.isTokenExpired(jwtToken)) {
                    write(response, HttpServletResponse.SC_UNAUTHORIZED,
                            GlobalApiResponse.fail("토큰이 만료되었습니다.", "JWT_EXPIRED"));
                    return;
                }

                if (!jwtUtils.isTokenValid(jwtToken, userDetails)) {
                    write(response, HttpServletResponse.SC_UNAUTHORIZED,
                            GlobalApiResponse.fail("유효하지 않은 토큰입니다.", "JWT_INVALID"));
                    return;
                }

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
            }
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED,
                    GlobalApiResponse.fail(e.getMessage(), "JWT_INVALID"));
        }
    }
}
