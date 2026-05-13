package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static nosql.params.ReviewParams.COUNT_FIELD;
import static nosql.params.ReviewParams.REVIEWS_FIELD;

public record ReviewsResponse(
        @JsonProperty(REVIEWS_FIELD)
        List<ReviewResponse> reviews,
        @JsonProperty(COUNT_FIELD)
        int count
) {
}
