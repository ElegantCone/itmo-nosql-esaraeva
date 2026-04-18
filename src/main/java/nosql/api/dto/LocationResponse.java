package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocationResponse(
        @JsonProperty("city")
        String city,
        @JsonProperty("address")
        String address
) {
}
