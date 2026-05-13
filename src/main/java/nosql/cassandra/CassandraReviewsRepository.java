package nosql.cassandra;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CassandraReviewsRepository extends CrudRepository<EventReview, ReviewKey> {
    EventReview findFirstByKeyEventIdAndKeyCreatedBy(String eventId, String createdBy);

    @NonNull
    List<EventReview> findReviewsByKeyEventId(String eventId);
}
