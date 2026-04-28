package nosql.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

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
    private final static String createdAtField = "created_at";
    private final static String updatedAtField = "updated_at";
    private final static String userField = "user_id";

    public String createSession() {
        for (var i = 0; i < 3; i++) {
            var sessionId = SessionIdUtils.generateSessionId();
            String key = prefix + sessionId;
            var now = Instant.now().toString();
            var created = redisTemplate.opsForHash().putAndExpire(
                    key,
                    Map.of(
                            createdAtField, now,
                            updatedAtField, now
                    ),
                    RedisHashCommands.HashFieldSetOption.upsert(),
                    Expiration.seconds(sessionTtl)
            );
            if (Boolean.TRUE.equals(created)) {
                return sessionId;
            }
        }
        throw new RuntimeException("Failed to create session");
    }

    public void refresh(String sessionId) {
        SessionIdUtils.validateSessionId(sessionId);
        redisTemplate.opsForHash().putAndExpire(
                prefix + sessionId,
                Map.of(
                        updatedAtField, Instant.now().toString()
                ),
                RedisHashCommands.HashFieldSetOption.upsert(),
                Expiration.seconds(sessionTtl)
        );
    }

    public boolean isSessionPresented(String sessionId) {
        return redisTemplate.hasKey(prefix + sessionId);
    }

    public void assignUser(String sessionId, String userId) {
        SessionIdUtils.validateSessionId(sessionId);
        redisTemplate.opsForHash().putAndExpire(
                prefix + sessionId,
                Map.of(
                        userField, userId,
                        updatedAtField, Instant.now().toString()
                ),
                RedisHashCommands.HashFieldSetOption.upsert(),
                Expiration.seconds(sessionTtl)
        );
    }

    public Optional<String> getUserId(String sessionId) {
        SessionIdUtils.validateSessionId(sessionId);
        var value = redisTemplate.opsForHash().get(prefix + sessionId, userField);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.toString());
    }

    public void deleteSession(String sessionId) {
        SessionIdUtils.validateSessionId(sessionId);
        redisTemplate.delete(prefix + sessionId);
    }
}
