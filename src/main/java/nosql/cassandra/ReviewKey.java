package nosql.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nosql.params.ReviewParams;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@Data
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyClass
public class ReviewKey {
    @PrimaryKeyColumn(name = ReviewParams.EVENT_ID_FIELD, type = PrimaryKeyType.PARTITIONED)
    private String eventId;

    @PrimaryKeyColumn(name = ReviewParams.CREATED_BY_FIELD, ordinal = 0)
    private String createdBy;
}
