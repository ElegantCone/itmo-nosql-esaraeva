package lab1.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class SessionRepository {

    private final StringRedisTemplate redisTemplate;
    @Getter
    @Value("${APP_USER_SESSION_TTL}")
    private Long sessionTtl;
    private final static String prefix = "sid:";
    public final static String PATH = "/";
    public final static String SESSION_KEY = "X-Session-Id";

    public String createSession() {
        for (var i = 0; i < 3; i++) {
            var sessionId = SessionIdUtils.generateSessionId();
            String key = prefix + sessionId;
            var now = Instant.now().toString();
            var created = redisTemplate.opsForHash().putAndExpire(
                    key,
                    Map.of(
                            "created_at", now,
                            "updated_at", now
                    ),
                    RedisHashCommands.HashFieldSetOption.upsert(),
                    Expiration.seconds(sessionTtl)
            );
            if (created) {
                return sessionId;
            }
        }
        throw new RuntimeException("Failed to create session");
    }

    public void refresh(String sessionId) {
        SessionIdUtils.validateSessionId(sessionId);
        redisTemplate.expire(prefix + sessionId, sessionTtl, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(prefix + sessionId, "updated_at", Instant.now().toString());
    }

    public boolean isSessionPresented(String sessionId) {
        return redisTemplate.hasKey(prefix + sessionId);
    }
}
