package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.model.CreateUserRequest;
import lab1.service.SessionService;
import lab1.service.UserService;
import lab1.utils.UserUtils;
import lab1.utils.UserUtils.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static lab1.api.ResponseUtils.*;
import static lab1.utils.UserUtils.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            var fullName = UserUtils.validateRequiredString(body.get(FULL_NAME_FIELD), FULL_NAME_FIELD);
            var username = UserUtils.validateRequiredString(body.get(USERNAME_FIELD), USERNAME_FIELD);
            var password = UserUtils.validateRequiredString(body.get(PASSWORD_FIELD), PASSWORD_FIELD);

            try {
                var userId = userService.create(new CreateUserRequest(fullName, username, password));
                var sessionId = sessionService.createFreshSession();
                sessionService.assignUser(sessionId, userId);
                return createdResponse(sessionService.buildCookie(sessionId), null);
            } catch (UserAlreadyExistsException exception) {
                return conflictResponse(request, exception.getMessage(), sessionService);
            }
        } catch (RequiredFieldInvalidException exception) {
            return invalidFieldResponse(request, exception.getMessage(), sessionService);
        }
    }
}
