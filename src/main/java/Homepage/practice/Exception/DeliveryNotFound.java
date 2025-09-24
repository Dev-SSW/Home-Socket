package Homepage.practice.Exception;

public class DeliveryNotFound extends RuntimeException {
    public DeliveryNotFound(String message) { super(message); }
}
