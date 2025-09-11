package Homepage.practice.Exception;

public class CategoryNotFound extends RuntimeException {
    public CategoryNotFound(String message) { super(message); }
}
