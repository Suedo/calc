package example.calc.generator;

import java.util.Random;

public class Util {

    public static String generateRandomExpression(int length) {
        Random random = new Random();
        String operators = "+-*/";
        StringBuilder expression = new StringBuilder();

        // todo: if length is even, string ends with an operator, which is not allowed
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) { // Add a number
                expression.append(random.nextInt(9) + 1); // Random digit from 1 to 9
            } else { // Add an operator
                expression.append(operators.charAt(random.nextInt(operators.length())));
            }
            expression.append(" ");
        }
        return expression.toString().trim();
    }
}
