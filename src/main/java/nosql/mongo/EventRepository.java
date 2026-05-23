package nosql.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<EventDocument, String> {
    String EVENT_PROPERTY = "_id";
}
