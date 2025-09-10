package Homepage.practice.User;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.User.DTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/admin/getAllUser")
    @Operation(summary = "전체 유저 정보 가져오기")
    public ResponseEntity<GlobalApiResponse<List<UserResponse>>> getAllUser() {
        List<UserResponse> listUserResponse = userService.getAllUser();
        return ResponseEntity.ok(GlobalApiResponse.success("전체 정보 조회 성공", listUserResponse));
    }

    @GetMapping("/user/getUser")
    @Operation(summary = "특정 유저 정보 가져오기")
    public ResponseEntity<GlobalApiResponse<UserResponse>> getUser(@AuthenticationPrincipal User user) {
        UserResponse userResponse = userService.getUser(user.getUsername());
        return ResponseEntity.ok(GlobalApiResponse.success("정보 조회 성공", userResponse));
    }

    @PutMapping("/user/updateUser")
    @Operation(summary = "유저 정보 수정하기 (비밀번호 제외)")
    public ResponseEntity<GlobalApiResponse<UserResponse>> updateUser(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(user.getUsername(), request);
        return ResponseEntity.ok(GlobalApiResponse.success("정보 수정 성공", userResponse));
    }

    @PutMapping("/user/updatePassword")
    @Operation(summary = "비밀번호 수정하기")
    public ResponseEntity<GlobalApiResponse<?>> updatePassword(@AuthenticationPrincipal User user,
                                                               @Valid @RequestBody UserPassUpdateRequest request) {
        userService.updatePassword(user.getUsername(), request);
        return ResponseEntity.ok(GlobalApiResponse.success("비밀번호 수정 성공", null));
    }

    @DeleteMapping("/user/deleteUser")
    @Operation(summary = "회원 탈퇴하기")
    public ResponseEntity<GlobalApiResponse<?>> deleteUser(@AuthenticationPrincipal User user) {
        userService.deleteUser(user.getUsername());
        return ResponseEntity.ok(GlobalApiResponse.success("회원 탈퇴 성공", null));
    }
}
