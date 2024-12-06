package example.calc.tester;

import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.annotation.Observed;
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
    private final ContextRegistry contextRegistry;

    public TestService(RestClient restClient, ExecutorService executor, ContextRegistry contextRegistry) {
        this.restClient = restClient;
        this.executor = executor;
        this.contextRegistry = contextRegistry;
    }

    public String testFlow() {
        try {
            log.info("testFlow: Current Thread: {}", Thread.currentThread());
            final Instant start = Instant.now();
            // Step 1: Generate a random expression
            //ContextSnapshot snapshot = ContextSnapshot.captureAll();
            ContextSnapshot snapshot = ContextSnapshotFactory.builder().contextRegistry(contextRegistry).build().captureAll();

            var expressionF = this.executor.submit(snapshot.wrap(this::generateExpression));
            var dummy1F = this.executor.submit(snapshot.wrap(this::generateExpression));
            var dummy2F = this.executor.submit(snapshot.wrap(this::generateExpression));


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

    @Observed
    private String generateExpression() {
        log.info("generateExpression: Current Thread: {}", Thread.currentThread());
        return restClient
                .get()
                .uri("/generate")
                .retrieve()
                .body(String.class);
    }
}