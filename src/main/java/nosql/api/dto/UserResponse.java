package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import static nosql.params.UserRequestParams.*;

public record UserResponse(
        @JsonProperty(USER_ID)
        String id,
        @JsonProperty(FULL_NAME_FIELD)
        String fullName,
        @JsonProperty(USERNAME_FIELD)
        String username
) {
}
