package example.calc.generator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static example.calc.generator.Util.generateRandomExpression;

@RestController
@RequestMapping("/generate")
public class GenerateController {

    @GetMapping
    public String generateExpression(@RequestParam(defaultValue = "15") int length) {
        return generateRandomExpression(length);
    }


}
