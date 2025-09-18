package Homepage.practice.Exception;

public class CartNotFound extends RuntimeException{
    public CartNotFound(String message) { super(message); }
}
