package example.calc.tester;


import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import example.demo.shared.rest.GenerateAPIService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;


@RestController
@RequestMapping("/test")
public class TestController {

    private final GenerateAPIService generateAPIService;
    @GrpcClient("evaluate-service")
    private EvaluateServiceGrpc.EvaluateServiceBlockingStub evaluateServiceClient;

    public TestController(GenerateAPIService generateAPIService) {
        this.generateAPIService = generateAPIService;
    }

    @GetMapping
    public ResponseEntity<String> testFlow() {
        try {
            final Instant start = Instant.now();
            // Step 1: Generate a random expression
            Response<String> response = generateAPIService.generateExpression().execute();
            String expression = response.body();

            // Step 2: Evaluate the expression via gRPC
            Evaluate.EvaluateRequest request = Evaluate.EvaluateRequest.newBuilder()
                    .setExpression(expression)
                    .build();

            var result = evaluateServiceClient.evaluate(request).getResult();
            return ResponseEntity.ok(String.format("generated {%s} and evaluated its value as {%.8f} in %d ms",
                    expression, result, Duration.between(start, Instant.now()).toMillis()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

