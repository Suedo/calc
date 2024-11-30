package example.calc.tester;


import example.demo.proto.Evaluate;
import example.demo.proto.EvaluateServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/test")
public class TestController {

    private final RestTemplate restTemplate;

    @GrpcClient("evaluate-service")
    private EvaluateServiceGrpc.EvaluateServiceBlockingStub evaluateServiceClient;

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public double testFlow() {
        // Step 1: Generate a random expression
        String expression = restTemplate.getForObject("http://generate-service/generate", String.class);

        // Step 2: Evaluate the expression via gRPC
        Evaluate.EvaluateRequest request = Evaluate.EvaluateRequest.newBuilder()
                .setExpression(expression)
                .build();

        return evaluateServiceClient.evaluate(request).getResult();
    }
}

