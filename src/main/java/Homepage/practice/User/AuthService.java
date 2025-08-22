package Homepage.practice.User;

import Homepage.practice.Exception.JwtInvalid;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Exception.UsernameAlreadyExists;
import Homepage.practice.User.DTO.JwtRequest;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.JWT.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
        User user = User.createUser(request, passwordEncoder);
        userRepository.save(user);
    }

    /** 로그인 */
    public JwtResponse login(LoginRequest request) {
        try {
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
        } catch (InternalAuthenticationServiceException e) {
            // 포장된 예외의 원인이 UserNotFound라면
            if (e.getCause() instanceof UserNotFound) {
                throw new UserNotFound("아이디에 해당하는 회원이 없습니다.");
            }
            // 아니라면 그대로 다시 던진다 (InternalAuthenticationServiceException)
            throw e;
        }
    }

    /** 토큰 재발급 (리프레시 토큰을 요청으로 받음) */
    public JwtResponse tokenRenew(JwtRequest refreshToken) {
        try {
            // 사용자 이름 추출
            String username = jwtUtils.extractUsername(refreshToken.getToken());

            // 사용자 조회
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));

            // Refresh Token 만료 여부 확인
            if (jwtUtils.isTokenExpired(refreshToken.getToken())) {
                throw new JwtInvalid("리프레시 토큰이 만료되었습니다.");
            }

            // Refresh Token 유효성 검증
            if (!jwtUtils.isTokenValid(refreshToken.getToken(), user)) {
                throw new JwtInvalid("유효하지 않은 리프레시 토큰입니다.");
            }

            // 새로운 Access Token + Refresh Token 발급
            String newAccessToken = jwtUtils.generateToken(user);
            String newRefreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            return JwtResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expirationTime("24Hr")
                    .build();
        } catch (JwtException e) {
            // 서명이 틀리거나 변조된 토큰
            throw new JwtInvalid("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    /** 토큰 유효성 수동 검사 */
    public void validateTest(JwtRequest validateToken) {
        try {
            // 사용자 이름 추출
            String username = jwtUtils.extractUsername(validateToken.getToken());

            // 사용자 조회
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));

            // validate Token 만료 여부 확인
            if (jwtUtils.isTokenExpired(validateToken.getToken())) {
                throw new JwtInvalid("토큰이 만료되었습니다.");
            }

            // validate Token 유효성 검증
            if (!jwtUtils.isTokenValid(validateToken.getToken(), user)) {
                throw new JwtInvalid("유효하지 않은 토큰입니다.");
            }
        } catch (JwtException e) {
            // 서명이 틀리거나 변조된 토큰
            throw new JwtInvalid("유효하지 않은 토큰입니다.");
        }
    }
}
