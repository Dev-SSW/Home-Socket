package Homepage.practice.User;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
