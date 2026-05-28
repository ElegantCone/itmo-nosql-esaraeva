package nosql.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class RedisHelper<T> {
    protected final StringRedisTemplate redisTemplate;
    public static final String EVENT_PREFIX = "event:";
    public static final String USER_PREFIX = "user:";
    public static final String RECOMMENDATIONS_SUFFIX = ":recomms";
    public static final String REACTIONS_SUFFIX = ":reactions";
    public static final String REVIEWS_SUFFIX = ":reviews";

    public T getObjects(String key, Function<Map<String, String>, T> function) {
        if (!redisTemplate.hasKey(key)) {
            return null;
        }
        return redisTemplate.opsForHash().entries(key).entrySet().stream()
                .collect(Collectors.collectingAndThen(
                                Collectors.toMap(
                                        e -> e.getKey().toString(),
                                        e -> e.getValue().toString()
                                ), function
                        )
                );
    }

    protected String buildKey(String prefix, String identifier, String suffix) {
        return prefix + identifier + suffix;
    }
}
