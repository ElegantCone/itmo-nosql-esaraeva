package lab1.utils;

public class EventUtils extends CommonUtils {
    enum EventType {
        MEETUP,
        CONCERT,
        EXHIBITION,
        PARTY,
        OTHER
    }

    public static String validateOptionalCategoryField(Object value, String fieldName) {
        var category = validateNullableString(value, fieldName);
        if (category == null) return null;
        try {
            EventType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FieldInvalidException(fieldName);
        }
        return category;
    }

    public static class DuplicateEventException extends RuntimeException {
        public DuplicateEventException() {
            super("Event already exists");
        }
    }

    public static class EventNotFoundException extends RuntimeException {
        public EventNotFoundException() {
            super("Not found");
        }
    }

    public static class EventEditForbiddenException extends RuntimeException {
        public EventEditForbiddenException() {
            super("Not found. Be sure that event exists and you are the organizer");
        }
    }
}
