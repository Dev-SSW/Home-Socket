package Homepage.practice.User.DTO;

import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthGoogleResponse implements OAuthGlobalResponse{
    private final Map<String, Object> attribute;

    public OAuthGoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvier() {
        return "google";
    }
    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
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
        return (String) attribute.get("picture");
    }
}
