package lab1.service;

import lab1.model.CreateUserRequest;
import lab1.mongo.UserDocument;
import lab1.mongo.UserRepository;
import lab1.utils.UserUtils.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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

    public Optional<UserDocument> authenticate(String username, String password) {

        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(password, user.get().getPasswordHash())) {
            return Optional.empty();
        }
        return user;
    }
}
