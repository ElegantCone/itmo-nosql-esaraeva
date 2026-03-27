package lab1.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LocationResponse(
        @JsonProperty("address")
        String address
) {
}
