package datart.server.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "datart.spark")
public class SparkConfig {

    private Set<String> staticProperties;

}
