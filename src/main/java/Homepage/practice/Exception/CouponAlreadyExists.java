package Homepage.practice.Exception;

public class CouponAlreadyExists extends RuntimeException {
    public CouponAlreadyExists(String message) { super(message); }
}
