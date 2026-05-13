package nosql.utils;

public class ReviewUtils extends CommonUtils {
    public static class ReviewAlreadyExistsException extends RuntimeException {
        public ReviewAlreadyExistsException() {
            super("Already exists");
        }
    }

    public static class ReviewEventNotFoundException extends RuntimeException {
        public ReviewEventNotFoundException() {
            super("Event not found");
        }
    }
}
