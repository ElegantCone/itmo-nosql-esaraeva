package lab1.redis;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class CookieRepository {

    private final StringRedisTemplate redisTemplate;
    @Value("${APP_USER_SESSION_TTL}")
    private Long sessionTtl;
    private final static String prefix = "sid:";
    private final static String path = "/";
    private final static String sessionKey = "X-Session-Id";

    public ResponseCookie createSession() {
        for (var i = 0; i < 3; i++) {
            var sessionId = SessionIdUtils.generateSessionId();
            String key = prefix + sessionId;
            var now = Instant.now().toString();

            boolean created = Boolean.TRUE.equals(redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                @NullMarked
                @SuppressWarnings("unchecked")
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.watch(key);
                    var exists = Boolean.TRUE.equals(operations.hasKey(key));
                    if (exists) {
                        operations.unwatch();
                        return false;
                    }
                    operations.multi();
                    operations.opsForHash().put(key, "created_at", now);
                    operations.opsForHash().put(key, "updated_at", now);
                    operations.expire(key, sessionTtl, TimeUnit.SECONDS);
                    var execResult = operations.exec();
                    return !execResult.isEmpty();
                }
            }));

            if (created) {
                return buildCookieResponse(sessionId);
            }
        }
        throw new RuntimeException("Failed to create session");
    }

    public ResponseCookie refresh(String sessionId) {
        SessionIdUtils.validateSessionId(sessionId);
        redisTemplate.expire(prefix + sessionId, sessionTtl, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(prefix + sessionId, "updated_at", Instant.now().toString());
        return buildCookieResponse(sessionId);
    }

    public ResponseCookie buildCookieResponse(String sessionId) {
        return ResponseCookie.from(sessionKey, sessionId)
                .httpOnly(true)
                .maxAge(sessionTtl)
                .path(path)
                .build();
    }

    public boolean isSessionPresented(Cookie cookie) {
        return redisTemplate.hasKey(prefix + cookie.getValue());
    }

    public Optional<Cookie> getCookie(Cookie[] cookies) {
        if (cookies == null) return Optional.empty();
        for (var cookie : cookies) {
            if (cookie.getName() == null) continue;
            if (cookie.getName().equals(sessionKey)) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }
}
