package nosql.model;

import java.util.Map;

public record UpdateReviewRequest(
        String comment,
        Integer rating
) {
    public static UpdateReviewRequest from(Map<String, Object> body) {
        return new UpdateReviewRequest(
                CreateReviewRequest.parseOptionalComment(body),
                CreateReviewRequest.parseOptionalRating(body)
        );
    }
}
