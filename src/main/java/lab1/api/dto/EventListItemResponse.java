package lab1.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lab1.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventListItemResponse {
    @JsonProperty("id")
    String id;
    @JsonProperty("title")
    String title;
    @JsonProperty("description")
    String description;
    @JsonProperty("location")
    LocationResponse location;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("created_by")
    String createdBy;
    @JsonProperty("started_at")
    String startedAt;
    @JsonProperty("finished_at")
    String finishedAt;

    public void validate() throws IllegalArgumentException {
        CommonUtils.validateStringField(id, "id");
        CommonUtils.validateStringField(title, "title");
        CommonUtils.validateStringField(description, "description");
        if (location == null) {
            throw new CommonUtils.FieldInvalidException("location");
        }
        CommonUtils.validatedDateTimeField(createdAt, "created_at");
        CommonUtils.validateStringField(createdBy, "created_by");
        CommonUtils.validatedDateTimeField(startedAt, "started_at");
        CommonUtils.validatedDateTimeField(finishedAt, "finished_at");
    }

}
