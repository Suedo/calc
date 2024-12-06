package example.calc.evaluator;

import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class EvaluatorGrpcServer extends EvaluateServiceGrpc.EvaluateServiceImplBase {

    private final EvaluatorService evaluatorService;
    private final PostfixEvaluationService postfixEvaluationService;

    public EvaluatorGrpcServer(EvaluatorService evaluatorService, PostfixEvaluationService postfixEvaluationService) {
        this.evaluatorService = evaluatorService;
        this.postfixEvaluationService = postfixEvaluationService;
    }

    @Override
    public void evaluate(Evaluate.EvaluateRequest request, StreamObserver<Evaluate.EvaluateResponse> responseObserver) {
        String expression = request.getExpression();
        log.info("Received evaluate request with expression: {}", expression);

        double result = evaluatorService.evaluate(expression);

        // Build the gRPC response
        Evaluate.EvaluateResponse evaluateResponse = Evaluate.EvaluateResponse.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(evaluateResponse);
        responseObserver.onCompleted();
    }
}
