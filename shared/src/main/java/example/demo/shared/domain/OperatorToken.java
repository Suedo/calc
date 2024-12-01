package example.demo.shared.domain;

// Record for Operator tokens
public record OperatorToken(char operator) implements Token {
    @Override
    public String toString() {
        return String.valueOf(operator);
    }
}
