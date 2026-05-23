package nosql.model;

import java.util.Map;

import static nosql.params.ReviewParams.COMMENT_FIELD;
import static nosql.params.ReviewParams.RATING_FIELD;
import static nosql.utils.CommonUtils.FieldInvalidException;

public record CreateReviewRequest(
        String comment,
        int rating
) {
    private static final int MAX_COMMENT_LENGTH = 300;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    public static CreateReviewRequest from(Map<String, Object> body) {
        return new CreateReviewRequest(
                parseRequiredComment(body.get(COMMENT_FIELD)),
                parseRequiredRating(body.get(RATING_FIELD))
        );
    }

    static String parseRequiredComment(Object value) {
        if (!(value instanceof String comment) || comment.length() > MAX_COMMENT_LENGTH) {
            throw new FieldInvalidException(COMMENT_FIELD);
        }
        return comment;
    }

    static Integer parseOptionalRating(Map<String, Object> body) {
        if (!body.containsKey(RATING_FIELD)) {
            return null;
        }
        return parseRequiredRating(body.get(RATING_FIELD));
    }

    static String parseOptionalComment(Map<String, Object> body) {
        if (!body.containsKey(COMMENT_FIELD)) {
            return null;
        }
        return parseRequiredComment(body.get(COMMENT_FIELD));
    }

    private static int parseRequiredRating(Object value) {
        if (!(value instanceof Integer rating) || rating < MIN_RATING || rating > MAX_RATING) {
            throw new FieldInvalidException(RATING_FIELD);
        }
        return rating;
    }
}
