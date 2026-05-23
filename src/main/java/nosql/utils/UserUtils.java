package nosql.utils;

public class UserUtils extends CommonUtils {

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException() {
            super("user already exists");
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException() {
            super("Not found");
        }

        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
