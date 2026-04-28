package nosql.service;

import jakarta.servlet.http.Cookie;
import nosql.CookieController;
import nosql.redis.SessionIdUtils;
import nosql.redis.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CookieController cookieController;

    public Optional<String> findExistingSessionId(Cookie[] cookies) {
        var optionalCookie = cookieController.getCookie(cookies);
        if (optionalCookie.isEmpty()) {
            return Optional.empty();
        }

        var sessionId = optionalCookie.get().getValue();
        try {
            SessionIdUtils.validateSessionId(sessionId);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        if (!sessionRepository.isSessionPresented(sessionId)) {
            return Optional.empty();
        }
        return Optional.of(sessionId);
    }

    public ResponseCookie getResponseCookieOrNull(Cookie[] cookies) {
        return findExistingSessionId(cookies).map(cookieController::buildCookieResponse).orElse(null);
    }

    public String createOrRefreshCookie(Cookie[] cookies) {
        var existingSession = refreshExistingSession(cookies);
        return existingSession.orElseGet(this::createFreshSession);
    }

    public Optional<String> refreshExistingSession(Cookie[] cookies) {
        var existingSession = findExistingSessionId(cookies);
        existingSession.ifPresent(sessionRepository::refresh);
        return existingSession;
    }

    public String createFreshSession() {
        return sessionRepository.createSession();
    }

    public void assignUser(String sessionId, String userId) {
        sessionRepository.assignUser(sessionId, userId);
    }

    public Optional<String> getUserId(String sessionId) {
        return sessionRepository.getUserId(sessionId);
    }

    public void deleteSession(String sessionId) {
        sessionRepository.deleteSession(sessionId);
    }

    public ResponseCookie buildCookie(String sessionId) {
        return cookieController.buildCookieResponse(sessionId);
    }

    public ResponseCookie buildExpiredCookie(String sessionId) {
        return cookieController.buildExpiredCookieResponse(sessionId);
    }
}
