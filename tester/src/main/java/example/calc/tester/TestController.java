package example.calc.tester;


import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    private final RestClient restClient;
    private final EvaluateServiceGrpc.EvaluateServiceBlockingStub evaluateServiceClient;

    public TestController(RestClient restClient,
                          @GrpcClient("evaluate-service") EvaluateServiceGrpc.EvaluateServiceBlockingStub evaluateServiceClient) {
        this.restClient = restClient;
        this.evaluateServiceClient = evaluateServiceClient;
    }

    @GetMapping
    public ResponseEntity<String> testFlow() {

        final Instant start = Instant.now();
        // Step 1: Generate a random expression
        String expression = generateExpression();
        log.info("Generated expression: {}", expression);

        // Step 2: Evaluate the expression via gRPC
        // Step 2.1: create the request format
        Evaluate.EvaluateRequest request = Evaluate.EvaluateRequest.newBuilder()
                .setExpression(expression)
                .build();

        // Step 2.2: Send request and evaluate the tokens
        var result = evaluateServiceClient.evaluate(request).getResult();

        final String resultString = String.format("generated {%s} and evaluated its value as {%.8f} in %d ms",
                expression, result, Duration.between(start, Instant.now()).toMillis());
        log.info(resultString);
        return ResponseEntity.ok(resultString);

    }


    public String generateExpression() {
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}

