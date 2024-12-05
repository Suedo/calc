package example.calc.evaluator;

import example.demo.shared.domain.NumberToken;
import example.demo.shared.domain.OperatorToken;
import example.demo.shared.domain.Token;
import example.demo.shared.proto.CalculatorGrpc;
import example.demo.shared.proto.CalculatorOuterClass;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Slf4j
@Service
public class PostfixEvaluationService {

    @GrpcClient("calculator-service")
    private CalculatorGrpc.CalculatorBlockingStub calculatorClient;


    public double evaluatePostfix(List<Token> tokens) {
        log.info("evaluatingPostfix: {}", tokens);
        Deque<Double> stack = new ArrayDeque<>();

        for (Token token : tokens) {
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