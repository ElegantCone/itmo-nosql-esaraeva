package nosql.service;

import nosql.api.dto.UserResponse;
import nosql.api.dto.UsersResponse;
import nosql.model.CreateUserRequest;
import nosql.model.UserSearchCriteria;
import nosql.mongo.UserDocument;
import nosql.mongo.UserRepository;
import nosql.params.UserRequestParams;
import nosql.utils.UserUtils.UserAlreadyExistsException;
import nosql.utils.UserUtils.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import static nosql.mongo.UserRepository.USER_PROPERTY;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public String create(CreateUserRequest request) {
        var user = UserDocument.builder()
                .fullName(request.fullName())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        try {
            return userRepository.save(user).getId();
        } catch (DuplicateKeyException exception) {
            throw new UserAlreadyExistsException();
        }
    }

    public UserDocument authenticate(String username, String password) {
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public UsersResponse findAll(UserSearchCriteria criteria) {
        var query = new Query().with(Sort.by(Sort.Direction.ASC, USER_PROPERTY));
        if (criteria.id() != null) {
            query.addCriteria(Criteria.where(USER_PROPERTY).is(criteria.id()));
        }
        if (criteria.name() != null) {
            var namePattern = ".*" + Pattern.quote(criteria.name()) + ".*";
            query.addCriteria(Criteria.where(UserRequestParams.FULL_NAME_FIELD).regex(namePattern));
        }
        if (criteria.offset() != null) {
            query.skip(criteria.offset());
        }
        if (criteria.limit() != null) {
            query.limit(criteria.limit());
        }

        var users = mongoTemplate.find(query, UserDocument.class).stream()
                .map(this::toResponse)
                .toList();
        return new UsersResponse(users, users.size());
    }

    public UserResponse findById(String id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(UserNotFoundException::new);
    }

    public void ensureExists(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
    }

    private UserResponse toResponse(UserDocument userDocument) {
        return new UserResponse(userDocument.getId(), userDocument.getFullName(), userDocument.getUsername());
    }
}
