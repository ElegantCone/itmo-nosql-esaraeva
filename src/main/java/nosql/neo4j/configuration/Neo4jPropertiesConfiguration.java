package nosql.neo4j.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(Neo4jProperties.class)
public class Neo4jPropertiesConfiguration {
}
