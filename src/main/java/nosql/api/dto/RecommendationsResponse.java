package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nosql.params.EventParams;

import java.util.List;

public record RecommendationsResponse(
        @JsonProperty(EventParams.EVENTS)
        List<EventListItemResponse> events
) {
}
