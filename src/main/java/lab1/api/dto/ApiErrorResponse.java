package lab1.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiErrorResponse(
        @JsonProperty("message")
        String message
) {
}
