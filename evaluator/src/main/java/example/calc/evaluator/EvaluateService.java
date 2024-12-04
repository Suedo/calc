package example.calc.evaluator;

import example.demo.shared.Utils.Serdes;
import example.demo.shared.domain.NumberToken;
import example.demo.shared.domain.OperatorToken;
import example.demo.shared.domain.Token;
import example.demo.shared.proto.CalculatorGrpc;
import example.demo.shared.proto.CalculatorOuterClass;
import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


@Slf4j
@GrpcService
public class EvaluateService extends EvaluateServiceGrpc.EvaluateServiceImplBase {

    private final Serdes serdes;
    private final RestClient restClient;
    private final CalculatorGrpc.CalculatorBlockingStub calculatorClient;

    public EvaluateService(Serdes serdes, RestClient restClient,
                           // the value must match the `name` in the grpcClient property: `grpc.client.<name>`
                           @GrpcClient("calculator-service") CalculatorGrpc.CalculatorBlockingStub calculatorClient) {
        this.serdes = serdes;
        this.restClient = restClient;
        this.calculatorClient = calculatorClient;
    }

    @Override
    public void evaluate(Evaluate.EvaluateRequest request, StreamObserver<Evaluate.EvaluateResponse> responseObserver) {
        String expression = request.getExpression();
        log.info("Received evaluate request with expression: {}", expression);

        // Step 1: Tokenize the expression (REST call to Tokenize Service)
        List<Token> tokens = tokenize(expression);
        log.info("Expression Tokens: {}", serdes.serialize(tokens));


        // Step 2: Evaluate the tokens (Postfix)
        assert tokens != null : "Tokens cannot be null";
        double result = evaluatePostfix(tokens);
        log.info("Evaluation Result: {}", result);

        // Build the gRPC response
        Evaluate.EvaluateResponse evaluateResponse = Evaluate.EvaluateResponse.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(evaluateResponse);
        responseObserver.onCompleted();

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

    private double evaluatePostfix(List<Token> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        for (Token token : postfix) {
            if (token instanceof NumberToken numberToken) {
                stack.push(numberToken.value());
            } else if (token instanceof OperatorToken operatorToken) {
                double b = stack.pop();
                double a = stack.pop();

                CalculatorOuterClass.CalculatorRequest request = CalculatorOuterClass.CalculatorRequest.newBuilder()
                        .setA(a)
                        .setB(b)
                        .setOperation(mapOperator(operatorToken))
                        .build();

                double result = calculatorClient.calculate(request).getResult();
                stack.push(result);
            }
        }
        return stack.pop();
    }

    private CalculatorOuterClass.CalculatorRequest.Operation mapOperator(OperatorToken operatorToken) {
        return switch (operatorToken.operator()) {
            case '+' -> CalculatorOuterClass.CalculatorRequest.Operation.ADD;
            case '-' -> CalculatorOuterClass.CalculatorRequest.Operation.SUBTRACT;
            case '*' -> CalculatorOuterClass.CalculatorRequest.Operation.MULTIPLY;
            case '/' -> CalculatorOuterClass.CalculatorRequest.Operation.DIVIDE;
            default -> throw new IllegalArgumentException("Unknown operator: " + operatorToken.operator());
        };
    }
}
