package lab1.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.JsonNodeFactory;

@RestController
public class ActuatorEndpoint {
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        var response = JsonNodeFactory.instance.objectNode();
        response.put("status", "ok");
        return ResponseEntity.ok().body(response);
    }
}
