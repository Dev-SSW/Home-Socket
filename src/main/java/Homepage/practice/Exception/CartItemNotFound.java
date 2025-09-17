package Homepage.practice.Exception;

public class CartItemNotFound extends RuntimeException {
    public CartItemNotFound(String message) { super(message); }
}
