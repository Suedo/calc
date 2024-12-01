package example.calc.evaluator;

import example.demo.shared.proto.CalculatorGrpc;
import example.demo.shared.proto.CalculatorOuterClass;
import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@GrpcService
public class EvaluateService extends EvaluateServiceGrpc.EvaluateServiceImplBase {

    private final RestTemplate restTemplate;
    private final CalculatorGrpc.CalculatorBlockingStub calculatorClient;

    public EvaluateService(RestTemplate restTemplate, @GrpcClient("calculator") CalculatorGrpc.CalculatorBlockingStub calculatorClient) {
        this.restTemplate = restTemplate;
        this.calculatorClient = calculatorClient;
    }

    @Override
    public void evaluate(Evaluate.EvaluateRequest request, StreamObserver<Evaluate.EvaluateResponse> responseObserver) {
        String expression = request.getExpression();

        // Step 1: Tokenize the expression (REST call to Tokenize Service)
        ResponseEntity<List<String>> response = restTemplate.exchange(
                "http://tokenize-service/tokenize",
                HttpMethod.POST,
                new HttpEntity<>(expression),
                new ParameterizedTypeReference<>() {
                }
        );
        List<String> tokens = response.getBody();

        // Step 2: Evaluate the tokens (Postfix)
        assert tokens != null : "Tokens cannot be null";
        double result = evaluatePostfix(tokens);

        // Build the gRPC response
        Evaluate.EvaluateResponse evaluateResponse = Evaluate.EvaluateResponse.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(evaluateResponse);
        responseObserver.onCompleted();
    }

    private double evaluatePostfix(List<String> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        for (String token : postfix) {
            if (token.matches("\\d+")) {
                stack.push(Double.parseDouble(token));
            } else { // Operator
                double b = stack.pop();
                double a = stack.pop();

                CalculatorOuterClass.CalculatorRequest request = CalculatorOuterClass.CalculatorRequest.newBuilder()
                        .setA(a)
                        .setB(b)
                        .setOperation(mapOperator(token))
                        .build();

                double result = calculatorClient.calculate(request).getResult();
                stack.push(result);
            }
        }
        return stack.pop();
    }

    private CalculatorOuterClass.CalculatorRequest.Operation mapOperator(String operator) {
        return switch (operator) {
            case "+" -> CalculatorOuterClass.CalculatorRequest.Operation.ADD;
            case "-" -> CalculatorOuterClass.CalculatorRequest.Operation.SUBTRACT;
            case "*" -> CalculatorOuterClass.CalculatorRequest.Operation.MULTIPLY;
            case "/" -> CalculatorOuterClass.CalculatorRequest.Operation.DIVIDE;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
