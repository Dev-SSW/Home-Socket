package Homepage.practice.Exception;

public class OrderNotCancel extends RuntimeException {
    public OrderNotCancel(String message) { super(message); }
}
