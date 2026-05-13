package nosql.redis;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import nosql.api.dto.EventReviewsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Repository
public class RedisReviewsRepository extends RedisHelper<EventReviewsResponse> {

    @Getter
    @Value("${APP_EVENT_REVIEWS_TTL}")
    private Long reviewsTtl;

    private static final String PREFIX = "event:";
    private static final String SUFFIX = ":reviews";
    public static final String countField = "count";
    public static final String ratingField = "rating";

    public RedisReviewsRepository(StringRedisTemplate redisTemplate) {
        super(redisTemplate);
    }

    public EventReviewsResponse save(String eventName, long count, double rating) {
        var reviews = new EventReviewsResponse(count, rating);
        var key = buildKey(eventName);
        var entity = Map.of(
                countField, String.valueOf(count),
                ratingField, String.valueOf(rating)
        );
        redisTemplate.opsForHash().putAll(key, entity);
        redisTemplate.expire(key, Duration.ofSeconds(reviewsTtl));
        return reviews;
    }

    public void remove(String eventName) {
        redisTemplate.delete(buildKey(eventName));
    }

    @Nullable
    public EventReviewsResponse getReviews(String eventName) {
        var key = buildKey(eventName);
        return getObjects(key, map -> new EventReviewsResponse(
                Long.parseLong(map.get(countField)),
                Double.parseDouble(map.get(ratingField))
        ));
    }

    private String buildKey(String eventName) {
        var md5 = DigestUtils.md5DigestAsHex(eventName.getBytes(StandardCharsets.UTF_8));
        return PREFIX + md5 + SUFFIX;
    }
}
