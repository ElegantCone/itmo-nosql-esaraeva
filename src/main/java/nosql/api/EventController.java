package nosql.api;

import jakarta.servlet.http.HttpServletRequest;
import nosql.api.dto.IdResponse;
import nosql.model.CreateEventRequest;
import nosql.model.CreateReviewRequest;
import nosql.model.EventSearchCriteria;
import nosql.model.UpdateEventRequest;
import nosql.model.UpdateReviewRequest;
import nosql.service.EventService;
import nosql.service.SessionService;
import nosql.utils.CommonUtils.FieldInvalidException;
import nosql.utils.CommonUtils.ParameterInvalidException;
import nosql.utils.EventUtils.DuplicateEventException;
import nosql.utils.EventUtils.EventEditForbiddenException;
import nosql.utils.EventUtils.EventNotFoundException;
import nosql.utils.ReviewUtils.ReviewAlreadyExistsException;
import nosql.utils.ReviewUtils.ReviewEventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static nosql.api.ResponseUtils.*;
import static nosql.params.RequestCommonParams.LIMIT_PARAM;
import static nosql.params.RequestCommonParams.OFFSET_PARAM;
import static nosql.utils.CommonUtils.parseOptionalUnsignedInt;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SessionService sessionService;

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(HttpServletRequest request, @RequestBody Map<String, String> body) {
        var sessionId = getAndRefreshSession(request);
        if (sessionId == null) {
            return unauthorizedEmptyResponse();
        }
        var userId = sessionService.getUserId(sessionId);
        if (userId.isEmpty()) {
            return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
        }
        sessionService.refreshExistingSession(request.getCookies());
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
        var sessionId = getAndRefreshSession(request);
        if (sessionId == null) {
           return unauthorizedEmptyResponse();
        }
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
    public ResponseEntity<?> getEventById(HttpServletRequest request, @PathVariable("id") String id, @RequestParam Map<String, String> params) {
        try {
            return okResponse(sessionService.getResponseCookieOrNull(request.getCookies()), eventService.findById(id, EventSearchCriteria.from(params)));
        } catch (EventNotFoundException exception) {
            return notFoundResponse(request, exception.getMessage(), sessionService);
        }
    }

    @PostMapping("/events/{id}/like")
    public ResponseEntity<?> likeEvent(HttpServletRequest request, @PathVariable("id") String id) {
        return react(request, id, true, false);
    }

    @PostMapping("/events/{id}/dislike")
    public ResponseEntity<?> dislikeEvent(HttpServletRequest request, @PathVariable("id") String id) {
        return react(request, id, false, true);
    }

    @PostMapping("/events/{id}/reviews")
    public ResponseEntity<?> createReview(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body
    ) {
        var sessionId = getAndRefreshSession(request);
        if (sessionId == null) {
            return unauthorizedEmptyResponse();
        }
        var userId = sessionService.getUserId(sessionId);
        if (userId.isEmpty()) {
            return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
        }

        try {
            var reviewId = eventService.createReview(id, CreateReviewRequest.from(body), userId.get());
            return createdResponse(sessionService.buildCookie(sessionId), new IdResponse(reviewId));
        } catch (FieldInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        } catch (ReviewAlreadyExistsException exception) {
            return conflictResponse(exception.getMessage(), sessionService.buildCookie(sessionId));
        } catch (ReviewEventNotFoundException exception) {
            return notFoundResponse(sessionService.buildCookie(sessionId), exception.getMessage());
        }
    }

    @GetMapping("/events/{id}/reviews")
    public ResponseEntity<?> getReviews(
            HttpServletRequest request,
            @PathVariable("id") String id,
            @RequestParam Map<String, String> params
    ) {
        try {
            var limit = parseOptionalUnsignedInt(params.get(LIMIT_PARAM), LIMIT_PARAM);
            var offset = parseOptionalUnsignedInt(params.get(OFFSET_PARAM), OFFSET_PARAM);
            return okResponse(
                    sessionService.getResponseCookieOrNull(request.getCookies()),
                    eventService.findReviews(id, limit, offset)
            );
        } catch (FieldInvalidException | ParameterInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        } catch (ReviewEventNotFoundException exception) {
            return notFoundResponse(request, exception.getMessage(), sessionService);
        }
    }

    @PatchMapping("/events/{eventId}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            HttpServletRequest request,
            @PathVariable("eventId") String eventId,
            @PathVariable("reviewId") String reviewId,
            @RequestBody Map<String, Object> body
    ) {
        var sessionId = getAndRefreshSession(request);
        if (sessionId == null) {
            return unauthorizedEmptyResponse();
        }
        var userId = sessionService.getUserId(sessionId);
        if (userId.isEmpty()) {
            return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
        }

        try {
            eventService.updateReview(eventId, reviewId, UpdateReviewRequest.from(body), userId.get());
            return noContentResponse(sessionService.buildCookie(sessionId));
        } catch (FieldInvalidException exception) {
            return invalidResponse(request, exception.getMessage(), sessionService);
        } catch (ReviewEventNotFoundException exception) {
            return notFoundResponse(sessionService.buildCookie(sessionId), exception.getMessage());
        }
    }

    private ResponseEntity<?> react(HttpServletRequest request, String id, boolean isLiked, boolean expireOnUnauthorized) {
        try {
            var sessionId = sessionService.refreshExistingSession(request.getCookies()).orElse(null);
            if (sessionId == null) {
                if (expireOnUnauthorized) {
                    return unauthorizedEmptyResponse(sessionService.buildExpiredCookie(null));
                }
                return unauthorizedEmptyResponse();
            }
            var userId = sessionService.getUserId(sessionId);
            if (userId.isEmpty()) {
                if (expireOnUnauthorized) {
                    return unauthorizedEmptyResponse(sessionService.buildExpiredCookie(sessionId));
                }
                return unauthorizedEmptyResponse(sessionService.buildCookie(sessionId));
            }
            eventService.react(id, userId.get(), isLiked);
            return noContentResponse(sessionService.buildCookie(sessionId));
        } catch (EventNotFoundException exception) {
            return notFoundResponse(request, exception.getMessage(), sessionService);
        }
    }

    private String getAndRefreshSession(HttpServletRequest request) {
        var sessionId = sessionService.findExistingSessionId(request.getCookies()).orElse(null);
        if (sessionId != null) {
            sessionService.refreshExistingSession(request.getCookies());
        }
        return sessionId;
    }
}
