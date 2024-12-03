package example.demo.shared.rest;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


/**
 * public interface GenerateAPIService {
 *
 * @GET("/generate") Call<String> generateExpression();
 * }
 */

@Service
public class GenerateApiService {

    private final RestClient restClient;

    public GenerateApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String generate() {
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}