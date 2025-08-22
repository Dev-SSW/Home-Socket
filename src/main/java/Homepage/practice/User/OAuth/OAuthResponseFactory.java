package Homepage.practice.User.OAuth;

import Homepage.practice.Exception.OAuthProviderNotFound;
import Homepage.practice.User.DTO.OAuthGlobalResponse;
import Homepage.practice.User.DTO.OAuthGoogleResponse;
import Homepage.practice.User.DTO.OAuthNaverResponse;

import java.util.Map;

public class OAuthResponseFactory {
    public static OAuthGlobalResponse create(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return new OAuthGoogleResponse(attributes);
        } else if ("naver".equalsIgnoreCase(provider)) {
            return new OAuthNaverResponse(attributes);
        } else if ("kakao".equalsIgnoreCase(provider)) {
            // 카카오 추가 시, 여기에 새로운 DTO를 추가
            // return new OAuthKakaoResponse(attributes);
        }
        throw new OAuthProviderNotFound("해당 provider(" + provider + ")가 존재하지 않습니다.");
    }
}
