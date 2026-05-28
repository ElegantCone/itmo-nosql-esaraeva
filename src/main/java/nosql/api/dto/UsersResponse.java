package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static nosql.params.ReviewParams.COUNT_FIELD;
import static nosql.params.UserRequestParams.USERS_PARAM;

public record UsersResponse(
        @JsonProperty(USERS_PARAM)
        List<UserResponse> users,
        @JsonProperty(COUNT_FIELD)
        int count
) {
}
