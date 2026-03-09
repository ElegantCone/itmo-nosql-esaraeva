package lab1.api;

import jakarta.servlet.http.HttpServletRequest;
import lab1.redis.CookieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.JsonNodeFactory;

@RestController
@RequiredArgsConstructor
public class ActuatorEndpoint {

    private final CookieRepository cookieRepository;

    @GetMapping("/health")
    public ResponseEntity<?> health(HttpServletRequest request) {
        var body = JsonNodeFactory.instance.objectNode();
        body.put("status", "ok");
        var response = ResponseEntity.ok();
        var cookies = request.getCookies();
        var optionalCookie = cookieRepository.getCookie(cookies);
        if (optionalCookie.isPresent()) {
            var cookie = optionalCookie.get();
            return response.header(HttpHeaders.SET_COOKIE, cookieRepository.buildCookieResponse(cookie.getValue()).toString()).body(body);
        }
        return ResponseEntity.ok().body(body);
    }

    @PostMapping("/session")
    public ResponseEntity<?> session(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            var cookie = cookieRepository.createSession();
            return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
        }
        var optionalCookie = cookieRepository.getCookie(cookies);
        if (optionalCookie.isPresent()) {
            var cookie = optionalCookie.get();
            if (cookieRepository.isSessionPresented(cookie)) {
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookieRepository.refresh(cookie.getValue()).toString()).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, cookieRepository.createSession().toString()).build();
    }
}
