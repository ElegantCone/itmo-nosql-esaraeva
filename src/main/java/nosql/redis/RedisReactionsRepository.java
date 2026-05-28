package nosql.redis;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import nosql.api.dto.ReactionsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Repository
public class RedisReactionsRepository extends RedisHelper<ReactionsResponse> {
    @Getter
    @Value("${APP_LIKE_TTL}")
    private Long likeTtl;
    public static final String likesField = "likes";
    public static final String dislikesField = "dislikes";

    public RedisReactionsRepository(StringRedisTemplate redisTemplate) {
        super(redisTemplate);
    }

    public void remove(String eventName) {
        var key = buildKey(eventName);
        redisTemplate.delete(key);
    }

    public ReactionsResponse save(String eventName, long likes, long dislikes) {
        var reactionsEntity = new ReactionsResponse(likes, dislikes);
        var key = buildKey(eventName);
        var reactions = Map.of(
                likesField, String.valueOf(likes),
                dislikesField, String.valueOf(dislikes)
        );
        redisTemplate.opsForHash().putAll(key, reactions);
        redisTemplate.expire(key, Duration.ofSeconds(likeTtl));
        return reactionsEntity;
    }

    public void updateEventReactions(String eventName, Boolean previousIsLike, boolean currentIsLike) {
        var key = buildKey(eventName);
        if (!redisTemplate.hasKey(key)) {
            return;
        }
        if (previousIsLike == null) {
            redisTemplate.opsForHash().increment(key, currentIsLike ? likesField : dislikesField, 1);
            redisTemplate.expire(key, Duration.ofSeconds(likeTtl));
            return;
        }
        if (previousIsLike != currentIsLike) {
            redisTemplate.opsForHash().increment(key, previousIsLike ? likesField : dislikesField, -1);
            redisTemplate.opsForHash().increment(key, currentIsLike ? likesField : dislikesField, 1);
        }
        redisTemplate.expire(key, Duration.ofSeconds(likeTtl));
    }

    @Nullable
    public ReactionsResponse getReactions(String eventName) {
        var key = buildKey(eventName);
        return getObjects(key, map -> new ReactionsResponse(
                Long.parseLong(map.get(likesField)),
                Long.parseLong(map.get(dislikesField))
        ));
    }

    private String buildKey(String eventName) {
        var md5 = DigestUtils.md5DigestAsHex(eventName.getBytes(StandardCharsets.UTF_8));
        return buildKey(EVENT_PREFIX, md5, REACTIONS_SUFFIX);
    }
}
