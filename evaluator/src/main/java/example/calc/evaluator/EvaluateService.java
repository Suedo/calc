package example.calc.evaluator;

import example.demo.shared.Utils.Serdes;
import example.demo.shared.domain.Token;
import example.demo.shared.proto.Evaluate;
import example.demo.shared.proto.EvaluateServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

import static example.demo.shared.Utils.Sleeper.sleep;

@Slf4j
@GrpcService
public class EvaluateService extends EvaluateServiceGrpc.EvaluateServiceImplBase {

    private final TokenizeService tokenizeService;
    private final PostfixEvaluationService postfixEvaluationService;
    private final Serdes serdes;

    public EvaluateService(TokenizeService tokenizeService, PostfixEvaluationService postfixEvaluationService, Serdes serdes) {
        this.tokenizeService = tokenizeService;
        this.postfixEvaluationService = postfixEvaluationService;
        this.serdes = serdes;
    }

    @Override
    public void evaluate(Evaluate.EvaluateRequest request, StreamObserver<Evaluate.EvaluateResponse> responseObserver) {
        String expression = request.getExpression();
        log.info("Received evaluate request with expression: {}", expression);

        sleep(100);

        // Step 1: Tokenize the expression (REST call to Tokenize Service)
        List<Token> tokens = tokenizeService.tokenize(expression);
        log.info("Expression Tokens: {}", serdes.serialize(tokens));


        // Step 2: Evaluate the tokens (Postfix)
        assert tokens != null : "Tokens cannot be null";
        double result = postfixEvaluationService.evaluatePostfix(tokens);
        log.info("Evaluation Result: {}", result);

        // Build the gRPC response
        Evaluate.EvaluateResponse evaluateResponse = Evaluate.EvaluateResponse.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(evaluateResponse);
        responseObserver.onCompleted();

    }
}
