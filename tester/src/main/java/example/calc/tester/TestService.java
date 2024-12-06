package example.calc.tester;

import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class TestService {

    @GrpcClient("evaluate-service")
    private EvaluateServiceGrpc.EvaluateServiceBlockingStub evaluateServiceClient;

    private final RestClient restClient;
    private final ExecutorService executor;

    public TestService(RestClient restClient, ExecutorService executor) {
        this.restClient = restClient;
        this.executor = executor;
    }

    public String testFlow() {
        try {
            final Instant start = Instant.now();
            // Step 1: Generate a random expression
            String expression = this.executor.submit(this::generateExpression).get();
            //dummy calls to check if virtual threads make it parallel
            String dummy1 = this.executor.submit(this::generateExpression).get();
            String dummy2 = this.executor.submit(this::generateExpression).get();

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
            return resultString;
        } catch (Exception e) {
            log.error("error", e);
            throw new RuntimeException(e);
        }
    }

    private String generateExpression() {
        log.info("Current Thread: {}", Thread.currentThread().getName());
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}