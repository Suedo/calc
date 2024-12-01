package example.calc.calculator;

import example.demo.shared.proto.CalculatorGrpc;
import example.demo.shared.proto.CalculatorOuterClass;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CalculatorService extends CalculatorGrpc.CalculatorImplBase {

    @Override
    public void calculate(CalculatorOuterClass.CalculatorRequest request,
                          StreamObserver<CalculatorOuterClass.CalculatorResponse> responseObserver) {
        double result = switch (request.getOperation()) {
            case ADD -> request.getA() + request.getB();
            case SUBTRACT -> request.getA() - request.getB();
            case MULTIPLY -> request.getA() * request.getB();
            case DIVIDE -> request.getA() / request.getB();
            default -> throw new IllegalArgumentException("Invalid operation");
        };

        CalculatorOuterClass.CalculatorResponse response = CalculatorOuterClass.CalculatorResponse.newBuilder()
                .setResult(result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
