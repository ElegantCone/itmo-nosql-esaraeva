package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.model.CreateUserRequest;
import lab1.model.UserSearchCriteria;
import lab1.service.EventService;
import lab1.service.SessionService;
import lab1.service.UserService;
import lab1.utils.CommonUtils.FieldInvalidException;
import lab1.utils.CommonUtils.ParameterInvalidException;
import lab1.utils.UserUtils.UserAlreadyExistsException;
import lab1.utils.UserUtils.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static lab1.api.ResponseUtils.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EventService eventService;
    private final SessionService sessionService;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            var createUserRequest = CreateUserRequest.from(body);
            try {
                var userId = userService.create(createUserRequest);
                var sessionId = sessionService.createFreshSession();
                sessionService.assignUser(sessionId, userId);
                return createdResponse(sessionService.buildCookie(sessionId), null);
            } catch (UserAlreadyExistsException exception) {
                return conflictResponse(request, exception.getMessage(), sessionService);
            }
        } catch (FieldInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpServletRequest request, @RequestParam Map<String, String> params) {
        try {
            return okResponse(sessionService.getResponseCookieOrNull(request.getCookies()), userService.findAll(UserSearchCriteria.from(params)));
        } catch (FieldInvalidException | ParameterInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(HttpServletRequest request, @PathVariable("id") String id) {
        try {
            return okResponse(sessionService.getResponseCookieOrNull(request.getCookies()), userService.findById(id));
        } catch (UserNotFoundException exception) {
            return notFoundResponse(request, exception.getMessage(), sessionService);
        }
    }

    @GetMapping("/users/{id}/events")
    public ResponseEntity<?> getUserEvents(HttpServletRequest request, @PathVariable("id") String id) {
        try {
            userService.ensureExists(id);
            return okResponse(sessionService.getResponseCookieOrNull(request.getCookies()), eventService.findByOrganizerId(id));
        } catch (UserNotFoundException exception) {
            return notFoundResponse(request, exception.getMessage(), sessionService);
        }
    }
}
