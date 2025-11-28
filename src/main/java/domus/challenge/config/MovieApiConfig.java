package domus.challenge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@ConfigurationProperties(prefix = "movie.api")
@Data
public class MovieApiConfig {

    private String baseUrl;
    private String searchPath;

    private int connectTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;

    private DataSize maxInMemorySize;

    private int retryMaxAttempts;
    private Duration retryBackoff;

    private boolean loggingEnabled;
}
