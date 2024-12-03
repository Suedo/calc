package example.demo.shared.rest;

import example.demo.shared.domain.Token;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TokenizeApiService {

    private final RestClient restClient;

    public TokenizeApiService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Token> tokenize(String expression) {
        return restClient
                .post()
                .uri("/tokenize")
                .body(expression)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }); // prevents type-erasure and helps Spring resolve the generic type (List<Token>) at runtime
    }
}