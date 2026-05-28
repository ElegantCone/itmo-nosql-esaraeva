package nosql.neo4j.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.neo4j")
public record Neo4jProperties(
        String url,
        String user,
        String password
) {
}
