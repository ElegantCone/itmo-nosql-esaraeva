package nosql.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nosql.service.EventService;
import nosql.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static nosql.api.ResponseUtils.okResponse;
import static nosql.api.ResponseUtils.unauthorizedEmptyResponse;

@RestController
@RequiredArgsConstructor
public class RecommendationsController {

    private final EventService eventService;
    private final SessionService sessionService;

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(HttpServletRequest request) {
        var sessionId = sessionService.refreshExistingSession(request.getCookies()).orElse(null);
        if (sessionId == null) {
            return unauthorizedEmptyResponse();
        }
        var userId = sessionService.getUserId(sessionId);
        if (userId.isEmpty()) {
            return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
        }
        return okResponse(
                sessionService.buildCookie(sessionId),
                eventService.findRecommendations(userId.get())
        );
    }
}
