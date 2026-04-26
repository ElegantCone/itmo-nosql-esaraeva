package nosql.cassandra;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CassandraReactionsRepository extends CrudRepository<Reaction, ReactionKey> {

    Reaction findByKeyEventIdAndKeyCreatedBy(String eventId, String createdBy);

    @NonNull
    List<Reaction> findReactionsByKeyEventId(String eventId);
}
