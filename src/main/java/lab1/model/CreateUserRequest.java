package lab1.model;

public record CreateUserRequest(String fullName,
                                String username,
                                String password
) {
}
