package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.api.dto.IdResponse;
import lab1.model.CreateEventRequest;
import lab1.model.EventSearchCriteria;
import lab1.service.EventService;
import lab1.service.SessionService;
import lab1.utils.CommonUtils;
import lab1.utils.CommonUtils.RequiredFieldInvalidException;
import lab1.utils.EventUtils.DuplicateEventException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static lab1.api.ResponseUtils.*;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SessionService sessionService;

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(HttpServletRequest request, @RequestBody Map<String, String> body) {
        var sessionId = sessionService.createOrRefreshCookie(request.getCookies());
        var userId = sessionService.getUserId(sessionId);
        if (userId.isEmpty()) {
            return unauthorizeResponse(sessionService.buildCookie(sessionId));
        }

        try {
            var eventId = eventService.create(
                    new CreateEventRequest(body),
                    userId.get()
            );
            return createdResponse(sessionService.buildCookie(sessionId), new IdResponse(eventId));
        } catch (DuplicateEventException exception) {
            return conflictResponse(exception.getMessage(), sessionService.buildCookie(sessionId));
        } catch (RequiredFieldInvalidException ex) {
            return invalidFieldResponse(request, ex.getMessage(), sessionService);
        }
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents(
            HttpServletRequest request,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "limit", required = false) String limit,
            @RequestParam(name = "offset", required = false) String offset
    ) {
        try {
            var parsedLimit = CommonUtils.parseUnsignedInt(limit, "limit");
            var parsedOffset = CommonUtils.parseUnsignedInt(offset, "offset");
            CommonUtils.validateRequiredString(title, "title");
            return okResponse(sessionService.getResponseCookie(request.getCookies()).orElse(null), eventService.findAll(new EventSearchCriteria(title, parsedLimit, parsedOffset)));
        } catch (RequiredFieldInvalidException exception) {
            return invalidFieldResponse(request, exception.getMessage(), sessionService);
        }
    }
}
