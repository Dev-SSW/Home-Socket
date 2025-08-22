package Homepage.practice.Exception;

public class OAuthProviderNotFound extends RuntimeException{
    public OAuthProviderNotFound(String message) {
        super(message);
    }
}
