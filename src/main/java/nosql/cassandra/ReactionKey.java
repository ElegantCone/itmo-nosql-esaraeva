package nosql.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nosql.params.ReactionParams;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@Data
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyClass
public class ReactionKey {
    @PrimaryKeyColumn(name = ReactionParams.EVENT_ID_PARAM, type = PrimaryKeyType.PARTITIONED)
    private String eventId;
    @PrimaryKeyColumn(name = ReactionParams.CREATED_BY_PARAM, ordinal = 0)
    private String createdBy;
}
