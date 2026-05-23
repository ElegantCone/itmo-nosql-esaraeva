package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IdResponse(
        @JsonProperty("id")
        String id
) {
}
