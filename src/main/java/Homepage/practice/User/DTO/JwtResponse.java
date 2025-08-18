package Homepage.practice.User.DTO;

import Homepage.practice.User.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)	// null 값은 응답에 포함하지 않음
@JsonIgnoreProperties(ignoreUnknown = true)	// 예기치 않은 필드 무시
public class JwtResponse {
    private String token;            // Access Token
    private String refreshToken;     // Refresh Token
    private String expirationTime;   // 만료 시간
}
