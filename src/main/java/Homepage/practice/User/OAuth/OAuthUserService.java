package Homepage.practice.User.OAuth;

import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.User.DTO.OAuthGlobalResponse;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthUserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    // 로그인 테스트 : http://localhost:8081/oauth2/authorization/제공자
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        // DefaultOAuth2UserService로부터 사용자 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(request);

        // 제공자 이름 받기 (클라이언트_등록_정보.등록ID)
        String provider = request.getClientRegistration().getRegistrationId();

        // 각 제공자 별로 처리
        OAuthGlobalResponse response = OAuthResponseFactory.create(provider, oAuth2User.getAttributes());

        // 사용자 ID를 제공자_ID 형식으로 변환
        String username = response.getProvier() +"_" + response.getProviderId();

        // 해당 ID 이미 존재하는지 확인
        if (userRepository.existsByUsername(username)) {
            // 사용자 정보 조회
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
            return user;
        } else {
            User newUser = User.createUser(username, response.getName());
            userRepository.save(newUser);
            return newUser;
        }
    }
}
