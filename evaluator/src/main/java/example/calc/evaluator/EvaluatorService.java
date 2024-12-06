package example.calc.evaluator;

import example.demo.shared.Utils.Serdes;
import example.demo.shared.domain.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import static example.demo.shared.Utils.Sleeper.sleep;

@Slf4j
@Service
public class EvaluatorService {

    private final Serdes serdes;
    private final RestClient restClient;
    private final PostfixEvaluationService postfixEvaluationService;


    public EvaluatorService(Serdes serdes, RestClient restClient, PostfixEvaluationService postfixEvaluationService) {
        this.serdes = serdes;
        this.restClient = restClient;
        this.postfixEvaluationService = postfixEvaluationService;
    }

    public Double evaluate(String expression) {
        sleep(100);

        // Step 1: Tokenize the expression (REST call to Tokenize Service)
        List<Token> tokens = tokenize(expression);
        log.info("Expression Tokens: {}", serdes.serialize(tokens));


        // Step 2: Evaluate the tokens (Postfix)
        assert tokens != null : "Tokens cannot be null";
        final double result = postfixEvaluationService.evaluatePostfix(tokens);
        log.info("Evaluation Result: {}", result);
        return result;
    }

    private List<Token> tokenize(String expression) {
        log.info("Current Thread: {}", Thread.currentThread().getName());
        return restClient
                .post()
                .uri("/tokenize")
                .body(expression)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }); // prevents type-erasure and helps Spring resolve the generic type (List<Token>) at runtime
    }
}