package nosql.model;

import java.util.Map;

import static nosql.params.EventParams.*;
import static nosql.utils.EventUtils.*;

public record UpdateEventRequest(
        String category,
        Integer price,
        String city
) {
    public static UpdateEventRequest from(Map<String, Object> body) {
        var category = validateOptionalCategoryField(body.get(CATEGORY_FIELD), CATEGORY_FIELD);
        var price = parseOptionalUnsignedInt(body.get(PRICE_FIELD), PRICE_FIELD);
        var city = validateOptionalEmptyString(body.get(CITY_FIELD), CITY_FIELD);
        return new UpdateEventRequest(category, price, city);
    }
}
