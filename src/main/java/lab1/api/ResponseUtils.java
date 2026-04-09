package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.api.dto.ApiErrorResponse;
import lab1.service.SessionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {
    public static ResponseEntity<?> invalidResponse(HttpServletRequest request, String message, SessionService sessionService) {
        var response = ResponseEntity.badRequest();
        sessionService.refreshExistingSession(request.getCookies())
                .ifPresent(sessionId -> response.header(HttpHeaders.SET_COOKIE, sessionService.buildCookie(sessionId).toString()));
        return response.body(new ApiErrorResponse(message));
    }

    public static ResponseEntity<?> conflictResponse(HttpServletRequest request, String message, SessionService sessionService) {
        var response = ResponseEntity.status(HttpStatus.CONFLICT);
        sessionService.refreshExistingSession(request.getCookies())
                .ifPresent(sessionId -> response.header(HttpHeaders.SET_COOKIE, sessionService.buildCookie(sessionId).toString()));
        return response.body(new ApiErrorResponse(message));
    }

    public static ResponseEntity<?> conflictResponse(String message, ResponseCookie cookie) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ApiErrorResponse(message));
    }

    public static ResponseEntity<?> createdResponse(ResponseCookie cookie, Object responseBody) {
        var builder = ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString());
        if (responseBody == null) {
            return builder.build();
        }
        return builder.body(responseBody);
    }

    public static ResponseEntity<?> noContentResponse(ResponseCookie cookie) {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    public static ResponseEntity<?> unauthorizeResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("invalid credentials"));
    }

    public static ResponseEntity<?> unauthorizedEmptyResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    public static ResponseEntity<?> unauthorizedEmptyResponse(ResponseCookie cookie) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    public static ResponseEntity<?> okResponse(ResponseCookie cookie, Object responseBody) {
        var response = ResponseEntity.ok();
        if (cookie != null) {
            response.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return response.body(responseBody);
    }

    public static ResponseEntity<?> notFoundResponse(HttpServletRequest request, String message, SessionService sessionService) {
        var response = ResponseEntity.status(HttpStatus.NOT_FOUND);
        sessionService.refreshExistingSession(request.getCookies())
                .ifPresent(sessionId -> response.header(HttpHeaders.SET_COOKIE, sessionService.buildCookie(sessionId).toString()));
        return response.body(new ApiErrorResponse(message));
    }

    public static ResponseEntity<?> notFoundResponse(ResponseCookie cookie, String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ApiErrorResponse(message));
    }
}
