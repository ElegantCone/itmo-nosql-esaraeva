package nosql.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.lang.Nullable;
import nosql.api.dto.RecommendationsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisRecommendationsRepository extends RedisHelper<RecommendationsResponse> {

    private static final String EVENTS_FIELD = "events";

    @Value("${app.recommendations.ttl}")
    private Long recommendationsTtl;

    private final ObjectMapper objectMapper;

    public RedisRecommendationsRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate);
        this.objectMapper = objectMapper;
    }

    public RecommendationsResponse save(String userId, RecommendationsResponse recommendations) {
        try {
            var key = buildKey(userId);
            redisTemplate.opsForHash().put(
                    key,
                    EVENTS_FIELD,
                    objectMapper.writeValueAsString(recommendations.events())
            );
            redisTemplate.expire(key, Duration.ofSeconds(recommendationsTtl));
            return recommendations;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize recommendations", exception);
        }
    }

    @Nullable
    public RecommendationsResponse getRecommendations(String userId) {
        var key = buildKey(userId);
        var events = redisTemplate.opsForHash().get(key, EVENTS_FIELD);
        if (events == null) {
            return null;
        }

        try {
            return new RecommendationsResponse(
                    objectMapper.readValue(
                            events.toString(),
                            new TypeReference<>() {
                            }
                    )
            );
        } catch (JsonProcessingException exception) {
            redisTemplate.delete(key);
            return null;
        }
    }

    private String buildKey(String userId) {
        return buildKey(USER_PREFIX, userId, RECOMMENDATIONS_SUFFIX);
    }
}
