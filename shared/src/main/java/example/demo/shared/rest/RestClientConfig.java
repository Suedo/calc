package example.demo.shared.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;

@Slf4j
@Configuration
public class RestClientConfig {

    private final RestClientProperties restClientProperties;

    public RestClientConfig(RestClientProperties restClientProperties) {
        this.restClientProperties = restClientProperties;
    }

    @Bean
    @Qualifier("virtualThreadHttpClient")
    public HttpClient httpClient(@Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutorService) {
        return HttpClient.newBuilder()
                .executor(virtualThreadExecutorService)
                .build();
    }


    @Bean
    public RestClient restClient(RestClient.Builder builder, @Qualifier("virtualThreadHttpClient") HttpClient httpClient) {
        log.info("Creating RestClient for {}", restClientProperties.getBaseUrl());
        builder.requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl(restClientProperties.getBaseUrl());


        if (restClientProperties.isProxyEnabled()) {
            // todo
            throw new UnsupportedOperationException();
        }

        return builder.build();
    }
}
