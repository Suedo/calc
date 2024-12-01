package example.calc.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

class UtilTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGenerateRandomExpression() {
        int length = 15;

        final String csv = IntStream.range(0, 100).boxed()
                .map(i -> Util.generateRandomExpression(length))
                .peek(System.out::println)
                .collect(Collectors.joining(","));

        System.out.println(csv);

    }
}