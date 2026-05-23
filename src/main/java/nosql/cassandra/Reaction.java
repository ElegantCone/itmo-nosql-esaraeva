package nosql.cassandra;

import lombok.*;
import nosql.params.ReactionParams;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(ReactionParams.TABLE_NAME)
public class Reaction {
    @PrimaryKey
    ReactionKey key;
    @CassandraType(type = CassandraType.Name.TINYINT)
    @Column(ReactionParams.LIKE_PARAM)
    private int likeValue;
    @Column(ReactionParams.CREATED_AT_PARAM)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private Timestamp createdAt;

    public boolean isLike() {
        return likeValue == 1;
    }

    public String getEventId() {
        return key.getEventId();
    }

    public String getCreatedBy() {
        return key.getCreatedBy();
    }

}
