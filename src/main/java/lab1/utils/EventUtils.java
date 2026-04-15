package lab1.utils;

public class EventUtils {
    public static final String TITLE_FIELD = "title";
    public static final String ADDRESS_FIELD = "address";
    public static final String STARTED_AT_FIELD = "started_at";
    public static final String FINISHED_AT_FIELD = "finished_at";
    public static final String DESCRIPTION_FIELD = "description";


    public static class DuplicateEventException extends RuntimeException {
        public  DuplicateEventException() {
            super("event already exists");
        }
    }
}
