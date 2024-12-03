package example.demo.shared.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private final RestClientProperties restClientProperties;

    public RestClientConfig(RestClientProperties restClientProperties) {
        this.restClientProperties = restClientProperties;
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        builder.baseUrl(restClientProperties.getBaseUrl());

        if (restClientProperties.isProxyEnabled()) {
            // todo
            throw new UnsupportedOperationException();
        }

        return builder.build();
    }
}
