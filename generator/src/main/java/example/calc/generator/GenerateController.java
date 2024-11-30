package example.calc.generator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/generate")
public class GenerateController {

    @GetMapping
    public String generateExpression(@RequestParam(defaultValue = "10") int length) {
        return generateRandomExpression(length);
    }

    private String generateRandomExpression(int length) {
        Random random = new Random();
        String operators = "+-*/";
        StringBuilder expression = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) { // Add a number
                expression.append(random.nextInt(9) + 1); // Random digit from 1 to 9
            } else { // Add an operator
                expression.append(operators.charAt(random.nextInt(operators.length())));
            }
        }
        return expression.toString();
    }
}
