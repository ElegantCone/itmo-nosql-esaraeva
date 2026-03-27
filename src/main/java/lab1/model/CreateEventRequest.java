package lab1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import static lab1.utils.CommonUtils.validatedDateTimeField;
import static lab1.utils.CommonUtils.validateStringField;
import static lab1.utils.EventUtils.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateEventRequest {
    private String title;
    private String address;
    private String startedAt;
    private String finishedAt;
    private String description;

    public CreateEventRequest(Map<String, String> body) {
        title = validateStringField(body.get(TITLE_FIELD), TITLE_FIELD);
        address = validateStringField(body.get(ADDRESS_FIELD), ADDRESS_FIELD);
        startedAt = body.get(STARTED_AT_FIELD);
        validatedDateTimeField(startedAt, STARTED_AT_FIELD);
        finishedAt = body.get(FINISHED_AT_FIELD);
        validatedDateTimeField(finishedAt, FINISHED_AT_FIELD);
        description = validateStringField(body.get(DESCRIPTION_FIELD), DESCRIPTION_FIELD);
    }
}
