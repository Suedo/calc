package example.calc.tokenizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"example.calc.tokenizer", "example.demo.shared"})
public class TokenizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenizerApplication.class, args);
    }

}
