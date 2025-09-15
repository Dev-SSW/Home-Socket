package Homepage.practice.Exception;

public class ItemOutOfStockException extends RuntimeException {
    public ItemOutOfStockException(String message) { super(message); }
}
