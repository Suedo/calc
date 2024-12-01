package example.calc.generator;

import example.demo.shared.domain.NumberToken;
import example.demo.shared.domain.OperatorToken;
import example.demo.shared.domain.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {


    public static List<Token> tokenize(String expression) {
        Pattern pattern = Pattern.compile("\\d+|[+\\-*/()]");
        Matcher matcher = pattern.matcher(expression);
        List<Token> tokens = new ArrayList<>();

        while (matcher.find()) {
            String match = matcher.group();
            if (match.matches("\\d+")) {
                tokens.add(new NumberToken(Double.parseDouble(match)));
            } else {
                tokens.add(new OperatorToken(match.charAt(0)));
            }
        }
        return tokens;
    }

    public static List<Token> convertToPostfix(List<Token> tokens) {
        Deque<OperatorToken> operatorStack = new ArrayDeque<>();
        List<Token> postfix = new ArrayList<>();

        // Shunting Yard Algorithm
        // for simplicity, not dealing with Brackets
        for (Token token : tokens) {
            if (token instanceof NumberToken numberToken) {
                postfix.add(numberToken);
            } else if (token instanceof OperatorToken operatorToken) {
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(operatorToken)) {
                    postfix.add(operatorStack.pop());
                }
                operatorStack.push(operatorToken);
            }
        }

        while (!operatorStack.isEmpty()) {
            postfix.add(operatorStack.pop());
        }

        return postfix;
    }

    private static int precedence(OperatorToken operatorToken) {
        return switch (operatorToken.operator()) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            default -> -1;
        };
    }
}
