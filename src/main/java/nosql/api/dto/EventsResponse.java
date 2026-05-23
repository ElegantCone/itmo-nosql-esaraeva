package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EventsResponse(
        @JsonProperty("events")
        List<EventListItemResponse> events,
        @JsonProperty("count")
        int count
) {
}
