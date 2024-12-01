package example.calc.generator;

import example.demo.shared.domain.Token;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static example.calc.generator.Util.convertToPostfix;
import static example.calc.generator.Util.tokenize;

@RestController
@RequestMapping("/tokenize")
public class TokenizeController {

    @PostMapping
    public List<Token> tokenizeExpression(@RequestBody String expression) {
        return convertToPostfix(tokenize(expression));
    }
}
