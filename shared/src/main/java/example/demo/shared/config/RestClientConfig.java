package example.demo.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class RestClientConfig {

    private final boolean isVirtualThreadEnabled;
    private final RestClientProperties restClientProperties;

    public RestClientConfig(
            @Value("${spring.threads.virtual.enabled}") boolean isVirtualThreadEnabled,
            RestClientProperties restClientProperties
    ) {
        this.isVirtualThreadEnabled = isVirtualThreadEnabled;
        this.restClientProperties = restClientProperties;
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        log.info("Creating RestClient for {}", restClientProperties.getBaseUrl());

        if (isVirtualThreadEnabled) {
            // separate Executor for REST calls ? might help in avoiding contention ?
            final HttpClient httpClient = HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor()).build();
            builder = builder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
        }
        if (restClientProperties.isProxyEnabled()) {
            throw new UnsupportedOperationException(); // todo
        }

        builder.baseUrl(restClientProperties.getBaseUrl());
        return builder.build();
    }
}
