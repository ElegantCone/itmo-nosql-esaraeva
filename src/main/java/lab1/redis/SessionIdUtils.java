package lab1.redis;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class SessionIdUtils {

    private static final SecureRandom secureRandom = new SecureRandom();
    private final static Pattern sessionIdPattern = Pattern.compile("^[a-f0-9]{32}$");

    public static String generateSessionId() {
        byte[] bytes = new byte[16];
        StringBuilder sessionIdBuilder = new StringBuilder();
        secureRandom.nextBytes(bytes);
        for (byte b : bytes) {
            sessionIdBuilder.append(String.format("%02x", b));
        }
        return sessionIdBuilder.toString();
    }

    public static void validateSessionId(String sessionId) {
        if (!sessionIdPattern.matcher(sessionId).matches()) {
            throw new IllegalArgumentException("Invalid session ID format");
        }
    }
}
