package example.calc.calculator;

import example.demo.shared.exceptions.DivisionByZeroException;
import example.demo.shared.proto.CalculatorGrpc;
import example.demo.shared.proto.CalculatorOuterClass;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import static example.demo.shared.Utils.Sleeper.sleep;

@Slf4j
@GrpcService
public class CalculatorService extends CalculatorGrpc.CalculatorImplBase {

    @Override
    public void calculate(CalculatorOuterClass.CalculatorRequest request,
                          StreamObserver<CalculatorOuterClass.CalculatorResponse> responseObserver) {

        final CalculatorOuterClass.CalculatorRequest.Operation operation = request.getOperation();
        final double a = request.getA();
        final double b = request.getB();

        log.info("CalculateOperation: {} > {}, {}", operation, a, b);

        sleep(10);

        try {
            double result = switch (operation) {
                case ADD -> a + b;
                case SUBTRACT -> a - b;
                case MULTIPLY -> a * b;
                case DIVIDE -> {
                    if (b == 0) throw new DivisionByZeroException("Division by zero is not allowed");
                    yield a / b;
                }
                default -> throw new IllegalArgumentException("Invalid operation");
            };


            CalculatorOuterClass.CalculatorResponse response = CalculatorOuterClass.CalculatorResponse.newBuilder()
                    .setResult(result)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (DivisionByZeroException e) {
            log.error("Division by zero error: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asException());
        }
    }
}
