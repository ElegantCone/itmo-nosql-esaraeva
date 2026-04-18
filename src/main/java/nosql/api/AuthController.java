package nosql.api;

import jakarta.servlet.http.HttpServletRequest;
import nosql.service.SessionService;
import nosql.service.UserService;
import nosql.utils.CommonUtils.FieldInvalidException;
import nosql.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static nosql.api.ResponseUtils.*;
import static nosql.params.UserRequestParams.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SessionService sessionService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            var username = UserUtils.validateStringField(body.get(USERNAME_FIELD), USERNAME_FIELD);
            var password = UserUtils.validateStringField(body.get(PASSWORD_FIELD), PASSWORD_FIELD);
            var user = userService.authenticate(username, password);
            if (user == null) {
                return unauthorizeResponse();
            }
            var sessionId = sessionService.createOrRefreshCookie(request.getCookies());
            sessionService.assignUser(sessionId, user.getId());
            return noContentResponse(sessionService.buildCookie(sessionId));
        } catch (FieldInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        var existingSessionId = sessionService.findExistingSessionId(request.getCookies()).orElse(null);
        if (existingSessionId != null) {
            sessionService.deleteSession(existingSessionId);
            return noContentResponse(sessionService.buildExpiredCookie(existingSessionId));
        }
        return unauthorizedEmptyResponse();
    }
}
