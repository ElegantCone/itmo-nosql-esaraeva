package nosql.cassandra.configuration;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CassandraConfiguration {

    @Bean
    public CqlSession cqlSession(CassandraProperties props) {
        var loader = DriverConfigLoader.programmaticBuilder()
                .withString(DefaultDriverOption.REQUEST_CONSISTENCY, props.consistency()).build();
        initializeKeyspaceIfNeeded(props, loader);
        return sessionBuilder(props, loader)
                .withKeyspace(CqlIdentifier.fromCql(props.keyspace()))
                .build();
    }

    private void initializeKeyspaceIfNeeded(CassandraProperties props, DriverConfigLoader loader) {
        try (var session = sessionBuilder(props, loader).build()) {
            var statement = SchemaBuilder.createKeyspace(CqlIdentifier.fromCql(props.keyspace()))
                    .ifNotExists()
                    .withSimpleStrategy(1)
                    .build();
            session.execute(statement);
        }
    }

    private CqlSessionBuilder sessionBuilder(CassandraProperties props, DriverConfigLoader loader) {
        var builder = CqlSession.builder()
                .withConfigLoader(loader)
                .withLocalDatacenter(props.localDatacenter());
        for (var host : props.hosts().split(",")) {
            builder.addContactPoint(new InetSocketAddress(host.trim(), props.port()));
        }
        if (props.username() != null && !props.username().isEmpty()) {
            builder.withAuthCredentials(props.username(), props.password());
        }
        return builder;
    }
}
