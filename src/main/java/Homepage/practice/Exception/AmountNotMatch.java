package Homepage.practice.Exception;

public class AmountNotMatch extends RuntimeException {
    public AmountNotMatch(String message) {
        super(message);
    }
}
