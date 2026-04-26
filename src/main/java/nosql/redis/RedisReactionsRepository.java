package nosql.redis;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RedisReactionsRepository {
    private final StringRedisTemplate redisTemplate;
    @Getter
    @Value("${APP_LIKE_TTL}")
    private Long likeTtl;
    private final static String prefix = "events:";
    public static final String suffix = ":reactions";
    public static final String likesField = "likes";
    public static final String dislikesField = "dislikes";

    public void remove(String eventName) {
        var key = buildKey(eventName);
        redisTemplate.delete(key);
    }

    public Map<String, Long> save(String eventName, long likes, long dislikes) {
        var key = buildKey(eventName);
        var reactions = Map.of(
                likesField, String.valueOf(likes),
                dislikesField, String.valueOf(dislikes)
        );
        redisTemplate.opsForHash().putAndExpire(
                key,
                reactions,
                RedisHashCommands.HashFieldSetOption.upsert(),
                Expiration.seconds(likeTtl)
        );
        return Map.of(
                likesField, likes,
                dislikesField, dislikes
        );
    }

    @Nullable
    public Map<String, Long> getReactions(String eventName) {
        var key = buildKey(eventName);
        if (!redisTemplate.hasKey(key)) {
            return null;
        }
        return redisTemplate.opsForHash().entries(key).entrySet().stream()
                .collect(Collectors.toMap(
                                e -> e.getKey().toString(),
                                e -> Long.parseLong(e.getValue().toString())
                        )
                );
    }

    private String buildKey(String eventName) {
        var md5 = DigestUtils.md5DigestAsHex(eventName.getBytes(StandardCharsets.UTF_8));
        return prefix + md5 + suffix;
    }
}
