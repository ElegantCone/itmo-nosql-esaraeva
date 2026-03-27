package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.api.dto.IdResponse;
import lab1.model.CreateEventRequest;
import lab1.model.EventSearchCriteria;
import lab1.service.EventService;
import lab1.service.SessionService;
import lab1.utils.CommonUtils.FieldInvalidException;
import lab1.utils.EventUtils.DuplicateEventException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static lab1.api.ResponseUtils.*;
import static lab1.utils.CommonUtils.parseUnsignedIntParameter;
import static lab1.utils.CommonUtils.validatedDateTimeParameter;

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
            return unauthorizedEmptyResponse();
        }

        try {
            var eventId = eventService.create(
                    new CreateEventRequest(body),
                    userId.get()
            );
            return createdResponse(sessionService.buildCookie(sessionId), new IdResponse(eventId));
        } catch (DuplicateEventException exception) {
            return conflictResponse(exception.getMessage(), sessionService.buildCookie(sessionId));
        } catch (FieldInvalidException ex) {
            return invalidResponse(request, ex.getMessage(), sessionService);
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
            Integer limitValue = null;
            Integer offsetValue = null;
            if (limit != null) {
               limitValue = parseUnsignedIntParameter(limit, "limit");
            }
            if (offset != null) {
                offsetValue = parseUnsignedIntParameter(offset, "offset");
            }
            if (title != null) {
                validatedDateTimeParameter(title, "title");
            }
            return okResponse(sessionService.getResponseCookie(request.getCookies()).orElse(null), eventService.findAll(new EventSearchCriteria(title, limitValue, offsetValue)));
        } catch (FieldInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        }
    }
}
