package lab1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import static lab1.utils.CommonUtils.validateRequiredString;
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
        title = validateRequiredString(body.get(TITLE_FIELD), TITLE_FIELD);
        address = validateRequiredString(body.get(ADDRESS_FIELD), ADDRESS_FIELD);
        startedAt = validateRequiredString(body.get(STARTED_AT_FIELD), STARTED_AT_FIELD);
        finishedAt = validateRequiredString(body.get(FINISHED_AT_FIELD), FINISHED_AT_FIELD);
        description = validateRequiredString(body.get(DESCRIPTION_FIELD), DESCRIPTION_FIELD);
    }
}
