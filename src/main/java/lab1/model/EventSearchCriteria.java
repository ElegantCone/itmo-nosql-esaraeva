package lab1.model;

import java.time.LocalDate;
import java.util.Map;

import static lab1.params.EventParams.*;
import static lab1.params.EventSearchParams.*;
import static lab1.params.RequestCommonParams.*;
import static lab1.utils.EventUtils.*;

public record EventSearchCriteria(
        String id,
        String title,
        String category,
        Integer priceFrom,
        Integer priceTo,
        String city,
        LocalDate dateFrom,
        LocalDate dateTo,
        String username,
        Integer limit,
        Integer offset
) {

    public static EventSearchCriteria from(Map<String, String> body) {
        Integer limitValue = parseOptionalUnsignedInt(body.get(LIMIT_PARAM), LIMIT_PARAM);
        Integer offsetValue = parseOptionalUnsignedInt(body.get(OFFSET_PARAM), OFFSET_PARAM);
        Integer priceFromValue = parseOptionalUnsignedInt(body.get(PRICE_FROM_PARAM), PRICE_FROM_PARAM);
        Integer priceToValue = parseOptionalUnsignedInt(body.get(PRICE_TO_PARAM), PRICE_TO_PARAM);
        LocalDate dateFromValue = parseOptionalDate(body.get(DATE_FROM_PARAM), DATE_FROM_PARAM);
        LocalDate dateToValue = parseOptionalDate(body.get(DATE_TO_PARAM), DATE_TO_PARAM);
        var id = validateNullableString(body.get(ID_FIELD), ID_FIELD);
        var title = validateNullableString(body.get(TITLE_FIELD), TITLE_FIELD);
        var category = validateOptionalCategoryField(body.get(CATEGORY_FIELD), CATEGORY_FIELD);
        var city = validateNullableString(body.get(CITY_FIELD), CITY_FIELD);
        var user = validateNullableString(body.get(USER_PARAM), USER_PARAM);
        return new EventSearchCriteria(
                id,
                title,
                category,
                priceFromValue,
                priceToValue,
                city,
                dateFromValue,
                dateToValue,
                user,
                limitValue,
                offsetValue
        );
    }
}
