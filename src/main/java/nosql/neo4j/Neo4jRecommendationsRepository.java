package nosql.neo4j;

import lombok.RequiredArgsConstructor;
import nosql.mongo.EventDocument;
import org.neo4j.driver.Driver;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.neo4j.driver.Values.parameters;

@Repository
@RequiredArgsConstructor
public class Neo4jRecommendationsRepository {

    private final Driver driver;

    private static final String ID = "id";
    private static final String LIKES = "likes";
    private static final String TITLE = "title";
    private static final String USER_ID = "userId";
    private static final String EVENT_ID = "eventId";

    public void saveUser(String userId) {
        try (var session = driver.session()) {
            session.executeWrite(transaction -> {
                transaction.run(
                        "MERGE (:User {id: $userId})",
                        parameters(USER_ID, userId)
                ).consume();
                return null;
            });
        }
    }

    public void saveEvent(EventDocument event) {
        try (var session = driver.session()) {
            session.executeWrite(transaction -> {
                transaction.run(
                        "MERGE (event:Event {id: $eventId}) SET event.title = $title",
                        parameters(EVENT_ID, event.getId(), TITLE, event.getTitle())
                ).consume();
                return null;
            });
        }
    }

    public void saveLike(String userId, String eventId) {
        try (var session = driver.session()) {
            session.executeWrite(transaction -> {
                transaction.run(
                        """
                        MATCH (user:User {id: $userId})
                        MATCH (event:Event {id: $eventId})
                        MERGE (user)-[:LIKED]->(event)
                        """,
                        parameters(USER_ID, userId, EVENT_ID, eventId)
                ).consume();
                return null;
            });
        }
    }

    public List<RecommendedEventRef> findRecommendedEvents(String userId) {
        try (var session = driver.session()) {
            return session.executeRead(transaction -> {
                var result = transaction.run(
                        """
                        MATCH (me:User {id: $userId})-[:LIKED]->(:Event)<-[:LIKED]-(other:User)-[:LIKED]->(rec:Event)
                        WHERE NOT (me)-[:LIKED]->(rec)
                        RETURN rec.id AS id, count(DISTINCT other) AS likes
                        ORDER BY likes DESC, id ASC
                        """,
                        parameters(USER_ID, userId)
                );
                return result.list(record -> new RecommendedEventRef(
                        record.get(ID).asString(),
                        record.get(LIKES).asLong()
                ));
            });
        }
    }
}
