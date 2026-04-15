package lab1.model;

import java.util.Map;

import static lab1.params.UserRequestParams.*;
import static lab1.utils.CommonUtils.validateStringField;

public record CreateUserRequest(String fullName,
                                String username,
                                String password
) {
    public static CreateUserRequest from(Map<String, String> body) {
        var fullName = validateStringField(body.get(FULL_NAME_FIELD), FULL_NAME_FIELD);
        var username = validateStringField(body.get(USERNAME_FIELD), USERNAME_FIELD);
        var password = validateStringField(body.get(PASSWORD_FIELD), PASSWORD_FIELD);
        return new CreateUserRequest(fullName, username, password);
    }
}
