package lab1.utils;

import java.time.OffsetDateTime;

public class CommonUtils {

    public static String validateRequiredString(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldInvalidException(fieldName);
        }
        return value;
    }

    public static void validateRequiredDateTime(String value, String fieldName) {
        if (value == null) {
            throw new RequiredFieldInvalidException(fieldName);
        }
        try {
            OffsetDateTime.parse(value);
        } catch (Exception exception) {
            throw new RequiredFieldInvalidException(fieldName);
        }
    }

    public static Integer parseUnsignedInt(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldInvalidException(fieldName);
        }
        try {
            var parsedValue = Integer.parseInt(value);
            if (parsedValue < 0) {
                throw new RequiredFieldInvalidException(fieldName);
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new RequiredFieldInvalidException(fieldName);
        }
    }

    public static class RequiredFieldInvalidException extends RuntimeException {
        public RequiredFieldInvalidException(String fieldName) {
            super("invalid " + fieldName + " field");
        }
    }
}
