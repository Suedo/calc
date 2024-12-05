package example.calc.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static example.calc.generator.Util.generateRandomExpression;

@Slf4j
@RestController
@RequestMapping("/generate")
public class GenerateController {

    @GetMapping
    public String generateExpression(@RequestParam(defaultValue = "15") int length) {
        var expression = generateRandomExpression(length);
        log.info("Generated random expression of length {}: {}", length, expression);
        return expression;
    }


}
