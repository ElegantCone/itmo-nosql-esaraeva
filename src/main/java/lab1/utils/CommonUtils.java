package lab1.utils;

import java.time.OffsetDateTime;

public class CommonUtils {

    public static String validateStringField(String value, String fieldName) {
        if (!checkIfStringIsCorrect(value)) {
            throw new FieldInvalidException(fieldName);
        }
        return value;
    }

    private static boolean checkIfStringIsCorrect(String value) {
        return value != null && !value.isBlank();
    }

    public static void validatedDateTimeField(String value, String fieldName) {
        if (!checkIfDateTimeIsCorrect(value)) {
            throw new FieldInvalidException(fieldName);
        }
    }

    public static void validatedDateTimeParameter(String value, String parameterName) {
        if (!checkIfDateTimeIsCorrect(value)) {
            throw new ParameterInvalidException(parameterName);
        }
    }

    private static boolean checkIfDateTimeIsCorrect(String value) {
        try {
            OffsetDateTime.parse(value);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static Integer parseUnsignedIntParameter(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new FieldInvalidException(fieldName);
        }
        try {
            var parsedValue = Integer.parseInt(value);
            if (parsedValue < 0) {
                throw new ParameterInvalidException(fieldName);
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new ParameterInvalidException(fieldName);
        }
    }

    public static class FieldInvalidException extends RuntimeException {
        public FieldInvalidException(String fieldName) {
            super("invalid " + fieldName + " field");
        }
    }

    public static class ParameterInvalidException extends RuntimeException {
        public ParameterInvalidException(String parameterName) {
            super("invalid " + parameterName + " parameter");
        }
    }
}
