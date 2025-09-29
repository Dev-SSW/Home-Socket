package Homepage.practice.Exception;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(String message) { super(message); }
}
