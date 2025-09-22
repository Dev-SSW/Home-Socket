package Homepage.practice.Exception;

public class CouponNotFound extends RuntimeException {
    public CouponNotFound(String message) { super(message); }
}
