package Homepage.practice.Exception;

public class CartAccessDenied extends RuntimeException {
    public CartAccessDenied(String message) { super(message); }
}
