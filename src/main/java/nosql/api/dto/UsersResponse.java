package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UsersResponse(
        @JsonProperty("users")
        List<UserResponse> users,
        @JsonProperty("count")
        int count
) {
}
