package nosql.utils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CommonUtils {

    public static String validateStringField(String value, String fieldName) {
        if (!checkIfStringIsCorrect(value)) {
            throw new FieldInvalidException(fieldName);
        }
        return value;
    }

    public static String validateNullableString(Object value, String fieldName) {
        if (value == null) return null;
        return validateStringField(value.toString(), fieldName);
    }

    public static String validateOptionalEmptyString(Object value, String fieldName) {
        if (value == null) return null;
        var str = value.toString();
        if (str.isEmpty()) return "";
        return validateStringField(str, fieldName);
    }

    private static boolean checkIfStringIsCorrect(String value) {
        return value != null && !value.isBlank();
    }

    public static void validatedDateTimeField(String value, String fieldName) {
        if (!checkIfDateTimeIsCorrect(value)) {
            throw new FieldInvalidException(fieldName);
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
            throw new ParameterInvalidException(fieldName);
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

    public static Integer parseOptionalUnsignedInt(Object value, String fieldName) {
        if (value == null) return null;
        return parseUnsignedIntParameter(value.toString(), fieldName);
    }

    public static LocalDate parseOptionalDate(String value, String fieldName) {
        if (value == null) return null;
        return parseDateParameter(value, fieldName);
    }

    public static LocalDate parseDateParameter(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ParameterInvalidException(fieldName);
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException exception) {
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
            super("invalid " + parameterName + " field");
        }
    }
}
