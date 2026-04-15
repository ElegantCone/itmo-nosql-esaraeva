package lab1.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("full_name")
        String fullName,
        @JsonProperty("username")
        String username
) {
}
