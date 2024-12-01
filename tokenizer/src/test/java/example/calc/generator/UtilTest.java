package example.calc.generator;

import example.demo.shared.domain.NumberToken;
import example.demo.shared.domain.OperatorToken;
import example.demo.shared.domain.Token;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

    private final static String infix = "4 * 9 - 6 + 3 + 6 * 8 / 6 + 9";
    private final static String postFix = "4 9 * 6 - 3 + 6 8 * 6 / + 9 +";
    private final static String operators = "+-*/";

    private final static List<Token> infixTokenList = convertToTokenList(infix);
    private final static List<Token> postfixTokenList = convertToTokenList(postFix);

    // todo: do a perf comparison between this and tokenize algorithm
    private static List<Token> convertToTokenList(String s) {
        List<Token> result = new ArrayList<>();
        for (String each : s.split(" ")) {
            result.add(operators.contains(String.valueOf(each.charAt(0)))
                    ? new OperatorToken(each.charAt(0))
                    : new NumberToken(Double.parseDouble(each)));
        }
        return result;
    }

    @Test
    void tokenize() {
        var infixTokens = Util.tokenize(infix);
        assertEquals(infixTokenList, infixTokens);

        var postfixTokens = Util.tokenize(postFix);
        assertEquals(postfixTokenList, postfixTokens);
    }

    @Test
    void convertToPostfix() {
        System.out.println(infixTokenList);
        List<Token> actualPostfixTokenList = Util.convertToPostfix(Util.tokenize(infix));
        System.out.println(actualPostfixTokenList.stream().map(Token::toString).collect(Collectors.joining(" ")));
        assertEquals(postfixTokenList, actualPostfixTokenList);
    }
}