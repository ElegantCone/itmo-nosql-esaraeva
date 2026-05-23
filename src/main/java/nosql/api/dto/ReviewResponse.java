package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import static nosql.params.EventParams.ID_FIELD;
import static nosql.params.ReviewParams.*;

public record ReviewResponse(
        @JsonProperty(ID_FIELD)
        String id,
        @JsonProperty(EVENT_ID_FIELD)
        String eventId,
        @JsonProperty(COMMENT_FIELD)
        String comment,
        @JsonProperty(CREATED_AT_FIELD)
        String createdAt,
        @JsonProperty(CREATED_BY_FIELD)
        String createdBy,
        @JsonProperty(RATING_FIELD)
        int rating,
        @JsonProperty(UPDATED_AT_FIELD)
        String updatedAt
) {
}
