package nosql.model;

import nosql.params.UserRequestParams;

import java.util.Map;

import static nosql.params.EventParams.ID_FIELD;
import static nosql.params.RequestCommonParams.LIMIT_PARAM;
import static nosql.params.RequestCommonParams.OFFSET_PARAM;
import static nosql.utils.CommonUtils.parseOptionalUnsignedInt;
import static nosql.utils.CommonUtils.validateNullableString;

public record UserSearchCriteria(
        String id,
        String name,
        Integer limit,
        Integer offset
) {

    public static UserSearchCriteria from(Map<String, String> params) {
        Integer limitValue = parseOptionalUnsignedInt(params.get(LIMIT_PARAM), LIMIT_PARAM);
        Integer offsetValue = parseOptionalUnsignedInt(params.get(OFFSET_PARAM), OFFSET_PARAM);
        var id = validateNullableString(params.get(ID_FIELD), ID_FIELD);
        var name = validateNullableString(params.get(UserRequestParams.NAME_PARAM), UserRequestParams.NAME_PARAM);
        return new UserSearchCriteria(id, name, limitValue, offsetValue);
    }
}
