package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.api.dto.IdResponse;
import lab1.model.CreateEventRequest;
import lab1.model.EventSearchCriteria;
import lab1.model.UpdateEventRequest;
import lab1.service.EventService;
import lab1.service.SessionService;
import lab1.utils.CommonUtils.FieldInvalidException;
import lab1.utils.CommonUtils.ParameterInvalidException;
import lab1.utils.EventUtils.DuplicateEventException;
import lab1.utils.EventUtils.EventEditForbiddenException;
import lab1.utils.EventUtils.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
        }

        try {
            var eventId = eventService.create(
                    CreateEventRequest.from(body),
                    userId.get()
            );
            return createdResponse(sessionService.buildCookie(sessionId), new IdResponse(eventId));
        } catch (DuplicateEventException exception) {
            return conflictResponse(exception.getMessage(), sessionService.buildCookie(sessionId));
        } catch (FieldInvalidException ex) {
            return invalidResponse(request, ex.getMessage(), sessionService);
        }
    }

    @PatchMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        var sessionId = sessionService.findExistingSessionId(request.getCookies()).orElse(null);
        if (sessionId == null) {
           return unauthorizedEmptyResponse();
        }
        sessionId = sessionService.createOrRefreshCookie(request.getCookies());
        var userId = sessionService.getUserId(sessionId);
        if (userId.isEmpty()) {
            return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
        }

        try {
            eventService.update(id, UpdateEventRequest.from(body), userId.get());
            return noContentResponse(sessionService.buildCookie(sessionId));
        } catch (FieldInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        } catch (EventEditForbiddenException exception) {
            return notFoundResponse(sessionService.buildCookie(sessionId), exception.getMessage());
        }
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents(
            HttpServletRequest request,
            @RequestParam Map<String, String> params
    ) {
        try {
            return okResponse(
                    sessionService.getResponseCookieOrNull(request.getCookies()),
                    eventService.findAll(EventSearchCriteria.from(params))
            );
        } catch (FieldInvalidException | ParameterInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        }
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventById(HttpServletRequest request, @PathVariable("id") String id) {
        try {
            return okResponse(sessionService.getResponseCookieOrNull(request.getCookies()), eventService.findById(id));
        } catch (EventNotFoundException exception) {
            return notFoundResponse(request, exception.getMessage(), sessionService);
        }
    }
}
