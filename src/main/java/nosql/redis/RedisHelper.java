package nosql.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class RedisHelper<T> {
    protected final StringRedisTemplate redisTemplate;

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
}
