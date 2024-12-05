package example.demo.shared.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


/**
 * public interface GenerateAPIService {
 *
 * @GET("/generate") Call<String> generateExpression();
 * }
 */

@Slf4j
@Service
public class GenerateApiService {

    private final RestClient restClient;

    public GenerateApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String generate() {
        log.info("Current Thread: {}", Thread.currentThread().getName());
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}