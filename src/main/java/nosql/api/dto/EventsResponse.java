package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nosql.params.EventParams;
import nosql.params.ReviewParams;

import java.util.List;

public record EventsResponse(
        @JsonProperty(EventParams.EVENTS)
        List<EventListItemResponse> events,
        @JsonProperty(ReviewParams.COUNT_FIELD)
        int count
) {
}
