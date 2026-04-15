package lab1.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<EventDocument, String> {
}
