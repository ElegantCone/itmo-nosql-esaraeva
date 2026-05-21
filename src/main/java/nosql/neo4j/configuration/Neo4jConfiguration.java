package nosql.neo4j.configuration;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfiguration {

    @Bean
    public Driver neo4jDriver(Neo4jProperties properties) {
        return GraphDatabase.driver(
                properties.url(),
                AuthTokens.basic(properties.user(), properties.password())
        );
    }
}
