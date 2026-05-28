package nosql.neo4j;

public record RecommendedEventRef(
        String eventId,
        long likes
) {
}
