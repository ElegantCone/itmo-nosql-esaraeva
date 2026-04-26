package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import nosql.utils.CommonUtils;

import static nosql.params.EventListItemParams.*;
import static nosql.params.EventParams.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventListItemResponse {
    @JsonProperty(ID_FIELD)
    String id;
    @JsonProperty(TITLE_FIELD)
    String title;
    @JsonProperty(CATEGORY_FIELD)
    String category;
    @JsonProperty(PRICE_FIELD)
    Integer price;
    @JsonProperty(DESCRIPTION_FIELD)
    String description;
    @JsonProperty(LOCATION_FIELD)
    LocationResponse location;
    @JsonProperty(CREATED_AT_FIELD)
    String createdAt;
    @JsonProperty(CREATED_BY_FIELD)
    String createdBy;
    @JsonProperty(STARTED_AT_FIELD)
    String startedAt;
    @JsonProperty(FINISHED_AT_FIELD)
    String finishedAt;
    @JsonProperty(REACTIONS_FIELD)
    ReactionsResponse reactions;

    public void validate() throws IllegalArgumentException {
        CommonUtils.validateStringField(id, ID_FIELD);
        CommonUtils.validateStringField(title, TITLE_FIELD);
        CommonUtils.validateStringField(description, DESCRIPTION_FIELD);
        if (location == null) {
            throw new CommonUtils.FieldInvalidException(LOCATION_FIELD);
        }
        CommonUtils.validatedDateTimeField(createdAt, CREATED_AT_FIELD);
        CommonUtils.validateStringField(createdBy, CREATED_BY_FIELD);
        CommonUtils.validatedDateTimeField(startedAt, STARTED_AT_FIELD);
        CommonUtils.validatedDateTimeField(finishedAt, FINISHED_AT_FIELD);
    }
}
