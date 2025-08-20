package Homepage.practice.Exception;

public class JwtInvalid extends RuntimeException{
    public JwtInvalid(String message) {
        super(message);
    }
}
