package example.calc.tester;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public ResponseEntity<String> testFlow() {
        String result = testService.testFlow();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<String> evaluateExpression(@RequestBody String expression) {
        try {
            String result = testService.evaluateExpression(expression);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error while evaluating expression", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
