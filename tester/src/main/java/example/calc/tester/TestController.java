package example.calc.tester;


import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;


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
        String expression = generate();

        // Step 2: Evaluate the expression via gRPC
        Evaluate.EvaluateRequest request = Evaluate.EvaluateRequest.newBuilder()
                .setExpression(expression)
                .build();

        var result = evaluateServiceClient.evaluate(request).getResult();
        return ResponseEntity.ok(String.format("generated {%s} and evaluated its value as {%.8f} in %d ms",
                expression, result, Duration.between(start, Instant.now()).toMillis()));

    }


    public String generate() {
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}

