package example.demo.shared.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "restclient")
public class RestClientProperties {

    private String baseUrl;
    private boolean proxyEnabled = false;
    private String proxyHost;
    private Integer proxyPort;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
}
