package Homepage.practice.User;

import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Exception.UsernameAlreadyExists;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.JWT.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    /** 회원 가입 */
    @Transactional
    public void signup(SignupRequest request) {
        // 해당 ID 이미 존재하는지 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExists("이미 존재하는 아이디 입니다.");
        }
        User user = User.createUser(request);
        userRepository.save(user);
    }

    /** 로그인 */
    public JwtResponse login(LoginRequest request) {
        // 사용자 인증
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 사용자 정보 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));

        // 인증이 성공 시 SecurityContetext에 저장
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        // JWT 생성
        var jwt = jwtUtils.generateToken(user);
        var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .expirationTime("24Hr")
                .build();
    }
}
