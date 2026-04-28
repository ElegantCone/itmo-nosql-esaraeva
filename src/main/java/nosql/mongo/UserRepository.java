package nosql.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    String USER_PROPERTY = "_id";

    Optional<UserDocument> findByUsername(String username);
}
