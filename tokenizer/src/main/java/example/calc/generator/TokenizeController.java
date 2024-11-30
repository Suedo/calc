package example.calc.generator;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/tokenize")
public class TokenizeController {

    @PostMapping
    public List<String> tokenizeExpression(@RequestBody String expression) {
        return convertToPostfix(tokenize(expression));
    }

    private List<String> tokenize(String expression) {
        Pattern pattern = Pattern.compile("\\d+|[+\\-*/()]");
        Matcher matcher = pattern.matcher(expression);
        List<String> tokens = new ArrayList<>();

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private List<String> convertToPostfix(List<String> tokens) {
        Deque<String> operatorStack = new ArrayDeque<>();
        List<String> postfix = new ArrayList<>();

        for (String token : tokens) {
            if (token.matches("\\d+")) {
                postfix.add(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.pop(); // Pop '('
            } else { // Operator
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(token)) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.push(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            postfix.add(operatorStack.pop());
        }
        return postfix;
    }

    private int precedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }
}
