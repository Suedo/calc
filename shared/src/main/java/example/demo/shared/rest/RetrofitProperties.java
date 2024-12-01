package example.demo.shared.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "retrofit")
public class RetrofitProperties {

    private String baseUrl = "";
    private boolean proxyEnabled = false;
    private String proxyHost;
    private Integer proxyPort;
}
