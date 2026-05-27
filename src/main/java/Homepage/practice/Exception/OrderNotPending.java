package Homepage.practice.Exception;

public class OrderNotPending extends RuntimeException {
    public OrderNotPending(String message) {
        super(message);
    }
}
