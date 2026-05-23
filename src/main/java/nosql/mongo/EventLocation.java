package nosql.mongo;

import org.springframework.data.mongodb.core.mapping.Field;

public record EventLocation(
        @Field("city")
        String city,
        @Field("address")
        String address
) {
}
