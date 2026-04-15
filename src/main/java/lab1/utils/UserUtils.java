package lab1.utils;

public class UserUtils extends CommonUtils {

    public static final String USERNAME_FIELD = "username";
    public static final String FULL_NAME_FIELD = "full_name";
    public static final String PASSWORD_FIELD = "password";

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException() {
            super("user already exists");
        }
    }
}
