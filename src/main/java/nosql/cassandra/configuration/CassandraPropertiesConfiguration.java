package nosql.cassandra.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraPropertiesConfiguration {
}
