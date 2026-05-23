package nosql.cassandra;

import lombok.*;
import nosql.params.ReviewParams;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(ReviewParams.TABLE_NAME)
public class EventReview {
    @PrimaryKey
    private ReviewKey key;

    @Column(ReviewParams.ID_FIELD)
    @CassandraType(type = CassandraType.Name.UUID)
    private UUID id;

    @Column(ReviewParams.RATING_FIELD)
    @CassandraType(type = CassandraType.Name.TINYINT)
    private int rating;

    @Column(ReviewParams.COMMENT_FIELD)
    private String comment;

    @Column(ReviewParams.CREATED_AT_FIELD)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private Timestamp createdAt;

    @Column(ReviewParams.UPDATED_AT_FIELD)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    private Timestamp updatedAt;

    public String getEventId() {
        return key.getEventId();
    }

    public String getCreatedBy() {
        return key.getCreatedBy();
    }
}
