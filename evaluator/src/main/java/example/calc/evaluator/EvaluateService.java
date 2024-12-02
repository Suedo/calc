package example.calc.evaluator;

import example.demo.shared.domain.NumberToken;
import example.demo.shared.domain.OperatorToken;
import example.demo.shared.domain.Token;
import example.demo.shared.proto.CalculatorGrpc;
import example.demo.shared.proto.CalculatorOuterClass;
import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import example.demo.shared.rest.TokenizeAPIService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@GrpcService
public class EvaluateService extends EvaluateServiceGrpc.EvaluateServiceImplBase {

    private final TokenizeAPIService tokenizeAPIService;
    private final CalculatorGrpc.CalculatorBlockingStub calculatorClient;

    public EvaluateService(TokenizeAPIService tokenizeAPIService,
                           // the value must match the `name` in the grpcClient property: `grpc.client.<name>`
                           @GrpcClient("calculator-service") CalculatorGrpc.CalculatorBlockingStub calculatorClient) {
        this.tokenizeAPIService = tokenizeAPIService;
        this.calculatorClient = calculatorClient;
    }

    @Override
    public void evaluate(Evaluate.EvaluateRequest request, StreamObserver<Evaluate.EvaluateResponse> responseObserver) {
        String expression = request.getExpression();

        try {
            // Step 1: Tokenize the expression (REST call to Tokenize Service)
            Response<List<Token>> response = tokenizeAPIService.tokenize(expression).execute();
            List<Token> tokens = response.body();

            // Step 2: Evaluate the tokens (Postfix)
            assert tokens != null : "Tokens cannot be null";
            double result = evaluatePostfix(tokens);

            // Build the gRPC response
            Evaluate.EvaluateResponse evaluateResponse = Evaluate.EvaluateResponse.newBuilder()
                    .setResult(result)
                    .build();

            responseObserver.onNext(evaluateResponse);
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
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
