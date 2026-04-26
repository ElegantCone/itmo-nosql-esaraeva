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
}
