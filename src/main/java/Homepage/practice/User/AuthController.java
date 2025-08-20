package Homepage.practice.User;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.DTO.JwtRequest;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원 관리 API")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/public/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<GlobalApiResponse<?>> signUp( @Valid @RequestBody SignupRequest request){
        authService.signup(request);
        return ResponseEntity.ok(GlobalApiResponse.success("회원가입 성공", null));
    }

    @PostMapping("/public/login")
    @Operation(summary = "로그인")
    public ResponseEntity<GlobalApiResponse<JwtResponse>> login( @Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request); // 서비스 단에서 예외 발생 가능
        return ResponseEntity.ok(GlobalApiResponse.success("로그인 성공", jwtResponse));
    }

    @PostMapping("/public/tokenRenew")
    @Operation(summary = "리프레시 토큰을 통한 액세스 + 리프레시 토큰 재발급")
    public ResponseEntity<GlobalApiResponse<JwtResponse>> tokenRenew( @Valid @RequestBody JwtRequest request) {
        JwtResponse jwtResponse = authService.tokenRenew(request);
        return ResponseEntity.ok(GlobalApiResponse.success("재발급 성공", jwtResponse));
    }

    @PostMapping("/public/validateTest")
    @Operation(summary = "토큰 유효성 수동 검사")
    public ResponseEntity<GlobalApiResponse<?>> validateTest( @Valid @RequestBody JwtRequest request) {
        authService.validateTest(request);
        return ResponseEntity.ok(GlobalApiResponse.success("유효한 토큰입니다.", null));
    }
}
