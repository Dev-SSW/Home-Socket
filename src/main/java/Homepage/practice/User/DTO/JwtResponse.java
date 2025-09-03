package Homepage.practice.User.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 필드는 응답에서 제외
public class JwtResponse {
    private String token;            // Access Token
    private String refreshToken;     // Refresh Token
    private String expirationTime;   // 만료 시간
}
