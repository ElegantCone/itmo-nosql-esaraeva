package nosql.cassandra.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cassandra")
public record CassandraProperties(
        String hosts,
        int port,
        String username,
        String password,
        String keyspace,
        String consistency,
        String localDatacenter
) {
}
