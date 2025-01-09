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
            log.info("testFlow: Current Thread: {}", Thread.currentThread());
            final Instant start = Instant.now();
            // Step 1: Generate a random expression
            var expressionF = this.executor.submit(this::generateExpression);
            var dummy1F = this.executor.submit(this::generateExpression); // dummy task to check parallel execution
            var dummy2F = this.executor.submit(this::generateExpression);

            // Step 2: Evaluate the expression via gRPC
            // Step 2.1: create the request format
            String expression = expressionF.get();
            log.info("testFlow: expression obtained: {}, Thread: {}", expression, Thread.currentThread());
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

    public String evaluateExpression(String expression) {
        try {
            log.info("Evaluating user expression: {}", expression);
            final Instant start = Instant.now();

            Evaluate.EvaluateRequest request = Evaluate.EvaluateRequest.newBuilder()
                    .setExpression(expression)
                    .build();

            double result = evaluateServiceClient.evaluate(request).getResult();
            final String resultString = String.format("expression {%s} evaluated to: {%.8f} in %d ms",
                    expression, result, Duration.between(start, Instant.now()).toMillis());
            log.info(resultString);
            return resultString;
        } catch (Exception e) {
            log.error("Evaluation failed for expression: {}", expression, e);
            throw new RuntimeException("Failed to evaluate expression: " + e.getMessage());
        }
    }

    private String generateExpression() {
        log.info("generateExpression: Current Thread: {}", Thread.currentThread());
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}