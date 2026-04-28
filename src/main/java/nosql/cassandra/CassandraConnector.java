package nosql.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import nosql.params.ReactionParams;
import org.springframework.stereotype.Component;

import static com.datastax.oss.driver.api.core.type.DataTypes.*;

@Component
@RequiredArgsConstructor
public class CassandraConnector {
    private final CqlSession session;

    @PostConstruct
    public void init() {
        createEventReactionsTableIfNeeded();
        createEventReactionsIndexesIfNeeded();
    }

    private void createEventReactionsTableIfNeeded() {
        var statement = SchemaBuilder.createTable(ReactionParams.TABLE_NAME)
                .ifNotExists()
                .withPartitionKey(ReactionParams.EVENT_ID_PARAM, TEXT)
                .withClusteringColumn(ReactionParams.CREATED_BY_PARAM, TEXT)
                .withColumn(ReactionParams.LIKE_PARAM, TINYINT)
                .withColumn(ReactionParams.CREATED_AT_PARAM, TIMESTAMP)
                .build();

        session.execute(statement);
    }

    private void createEventReactionsIndexesIfNeeded() {
        var likeIdxStatement = SchemaBuilder.createIndex("event_reactions_like_value_idx")
                .ifNotExists()
                .onTable(ReactionParams.TABLE_NAME)
                .andColumn(ReactionParams.LIKE_PARAM)
                .build();
        session.execute(likeIdxStatement);
        var createdByIdxStatement = SchemaBuilder.createIndex("event_reactions_created_by_idx")
                .ifNotExists()
                .onTable(ReactionParams.TABLE_NAME)
                .andColumn(ReactionParams.CREATED_BY_PARAM)
                .build();
        session.execute(createdByIdxStatement);
    }
}
