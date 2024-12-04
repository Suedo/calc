package example.calc.tokenizer;

import example.demo.shared.Utils.Serdes;
import example.demo.shared.domain.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tokenize")
public class TokenizeController {

    private final Serdes serdes;

    public TokenizeController(Serdes serdes) {
        this.serdes = serdes;
    }

    /**
     * Receives an infix expression as a string, tokenizes it,
     * and returns the tokenized expression in postfix notation.
     *
     * @param expression an infix expression as a string
     * @return a list of tokens representing the same expression in postfix notation
     */
    @PostMapping
    public List<Token> tokenizeExpression(@RequestBody String expression) {
        log.info("Tokenize request received: {}", expression);

        List<Token> tokens = Util.tokenize(expression);
        log.info("Tokens extracted: {}", serdes.serialize(tokens));

        var postfix = Util.convertToPostfix(tokens);
        log.info("Postfix expression: {}", serdes.serialize(postfix));

        return postfix;
    }
}
