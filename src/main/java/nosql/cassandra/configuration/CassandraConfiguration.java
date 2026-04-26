package nosql.cassandra.configuration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CassandraConfiguration {

    @Bean
    public CqlSession cqlSession(CassandraProperties props) {
        var loader = DriverConfigLoader.programmaticBuilder()
                .withString(DefaultDriverOption.REQUEST_CONSISTENCY, props.consistency()).build();
        var builder = CqlSession.builder()
                .withConfigLoader(loader)
                .withLocalDatacenter(props.localDatacenter())
                .withKeyspace(props.keyspace());
        for (var host : props.hosts().split(",")) {
            builder.addContactPoint(new InetSocketAddress(host.trim(), props.port()));
        }
        if (props.username() != null && !props.username().isEmpty()) {
            builder.withAuthCredentials(props.username(), props.password());
        }
        return builder.build();
    }
}
