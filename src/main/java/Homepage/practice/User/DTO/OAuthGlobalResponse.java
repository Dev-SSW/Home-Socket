package Homepage.practice.User.DTO;

public interface OAuthGlobalResponse {
    String getProvier();            // 제공자
    String getProviderId();          // 제공자에서 발급해준 ID
    String getEmail();              // 이메일
    String getName();               // 사용자 이름
    String getProfileImage();       // 프로필 이미지
}
