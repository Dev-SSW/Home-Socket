package Homepage.practice.User.DTO;

import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthNaverResponse implements OAuthGlobalResponse {
    private final Map<String, Object> attribute;

    public OAuthNaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvier() {
        return "naver";
    }
    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }
    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }
    @Override
    public String getName() {
        return attribute.get("name").toString();
    }
    @Override
    public String getProfileImage() {
        return (String) attribute.get("profile_image");
    }
}
