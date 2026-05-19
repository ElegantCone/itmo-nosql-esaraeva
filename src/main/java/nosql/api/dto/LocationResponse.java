package nosql.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static nosql.params.EventParams.ADDRESS_FIELD;
import static nosql.params.EventParams.CITY_FIELD;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocationResponse(
        @JsonProperty(CITY_FIELD)
        String city,
        @JsonProperty(ADDRESS_FIELD)
        String address
) {
}
