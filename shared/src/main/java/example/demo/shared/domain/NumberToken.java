package example.demo.shared.domain;

// Record for Number tokens
public record NumberToken(double value) implements Token {
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
