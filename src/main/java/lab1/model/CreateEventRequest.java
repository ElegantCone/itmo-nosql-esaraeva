package lab1.model;

import java.util.Map;

import static lab1.params.EventParams.*;
import static lab1.utils.CommonUtils.validateStringField;
import static lab1.utils.CommonUtils.validatedDateTimeField;

public record CreateEventRequest (
        String title,
        String address,
        String startedAt,
        String finishedAt,
        String description
) {
    public static CreateEventRequest from(Map<String, String> body) {
        var title = validateStringField(body.get(TITLE_FIELD), TITLE_FIELD);
        var address = validateStringField(body.get(ADDRESS_FIELD), ADDRESS_FIELD);
        var startedAt = body.get(STARTED_AT_FIELD);
        validatedDateTimeField(startedAt, STARTED_AT_FIELD);
        var finishedAt = body.get(FINISHED_AT_FIELD);
        validatedDateTimeField(finishedAt, FINISHED_AT_FIELD);
        var description = validateStringField(body.get(DESCRIPTION_FIELD), DESCRIPTION_FIELD);
        return new CreateEventRequest(title, address, startedAt, finishedAt, description);
    }
}
