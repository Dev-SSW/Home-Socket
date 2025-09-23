package Homepage.practice.Exception;

public class CouponExpired extends RuntimeException {
    public CouponExpired(String message) { super(message); }
}
