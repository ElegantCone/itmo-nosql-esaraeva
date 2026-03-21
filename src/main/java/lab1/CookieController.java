package lab1;

import jakarta.servlet.http.Cookie;
import lab1.redis.SessionIdUtils;
import lab1.redis.SessionRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CookieController {

    SessionRepository sessionRepository;


    public ResponseCookie buildCookieResponse(String sessionId) {
        SessionIdUtils.validateSessionId(sessionId);
        return ResponseCookie.from(SessionRepository.SESSION_KEY, sessionId)
                .httpOnly(true)
                .maxAge(sessionRepository.getSessionTtl())
                .path(SessionRepository.PATH)
                .build();
    }

    public Optional<Cookie> getCookie(Cookie[] cookies) {
        if (cookies == null) return Optional.empty();
        for (var cookie : cookies) {
            if (cookie.getName() == null) continue;
            if (cookie.getName().equals(SessionRepository.SESSION_KEY)) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }
}
