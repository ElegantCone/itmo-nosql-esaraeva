package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static nosql.params.ReviewParams.COUNT_FIELD;
import static nosql.params.ReviewParams.RATING_FIELD;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventReviewsResponse {
    @JsonProperty(COUNT_FIELD)
    private long count;
    @JsonProperty(RATING_FIELD)
    private double rating;
}
