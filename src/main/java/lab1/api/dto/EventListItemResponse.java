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
        CommonUtils.validateRequiredString(id, "id");
        CommonUtils.validateRequiredString(title, "title");
        CommonUtils.validateRequiredString(description, "description");
        if (location == null) {
            throw new CommonUtils.RequiredFieldInvalidException("location");
        }
        CommonUtils.validateRequiredDateTime(createdAt, "created_at");
        CommonUtils.validateRequiredString(createdBy, "created_by");
        CommonUtils.validateRequiredDateTime(startedAt, "started_at");
        CommonUtils.validateRequiredDateTime(finishedAt, "finished_at");
    }

}
