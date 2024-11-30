I want you to come up with a microservice based BODMAS rule calculator, using Java 21, SpringBoot 3.2, gRPC and Protobuf
and REST based technologies.

Below is the highlevel understanding of:

1. BODMAS rule
2. Java monolithic Code that calculates a random generated infix notation as per BODMAS rule
3. Desired Architechture of splitting the monolith into microservices, talking over rest and gRPC

## 1. BODMAS rule

The **BODMAS** rule is a mathematical convention that specifies the order of operations for solving expressions. It
stands for:

- **B**: Brackets (solve expressions within brackets first)
- **O**: Orders (handle exponents or roots, such as squares, square roots, etc.)
- **D**: Division (perform division next, from left to right)
- **M**: Multiplication (perform multiplication, from left to right)
- **A**: Addition (handle addition, from left to right)
- **S**: Subtraction (handle subtraction, from left to right)

The operations are prioritized in this order, with brackets taking the highest precedence and addition/subtraction the
lowest. When operations of the same priority (e.g., division and multiplication) appear, solve them **left to right**.

### Example

Evaluate:  
\[ 6 + 2 \times (3^2 - 1) \div 4 \]

1. Solve the brackets:  
   \[ 3^2 - 1 = 9 - 1 = 8 \]  
   The expression becomes:  
   \[ 6 + 2 \times 8 \div 4 \]

2. Handle orders: Already done in step 1.

3. Perform division and multiplication (left to right):  
   \[ 2 \times 8 = 16 \]  
   \[ 16 \div 4 = 4 \]  
   The expression becomes:  
   \[ 6 + 4 \]

4. Perform addition:  
   \[ 6 + 4 = 10 \]

**Answer**: 10

## 2. Core Java Monolithic Implementation

```java
import java.util._;
import java.util.regex._;
import java.util.stream.\*;

public class BODMASCalculator {

    public static void main(String[] args) {
        String expression = generateRandomExpression(10); // Generate random expression of size 10
        System.out.println("Generated Expression: " + expression);

        double result = evaluateExpression(expression);
        System.out.println("Result: " + result);
    }

    // Method 1: Generate a random expression
    public static String generateRandomExpression(int length) {
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

    // Method 2: Evaluate the expression
    public static double evaluateExpression(String expression) {
        List<Token> tokens = tokenizeExpression(expression);
        return evaluateTokens(tokens);
    }

    // Step 2.1: Tokenize the expression into numbers and operators
    private static List<Token> tokenizeExpression(String expression) {
        Pattern pattern = Pattern.compile("\\d+|[+\\-*/()]");
        Matcher matcher = pattern.matcher(expression);
        List<Token> tokens = new ArrayList<>();

        while (matcher.find()) {
            String match = matcher.group();
            if (match.matches("\\d+")) {
                tokens.add(new NumberToken(Double.parseDouble(match)));
            } else {
                tokens.add(new OperatorToken(match.charAt(0)));
            }
        }
        return tokens;
    }

    // Step 2.2: Evaluate the tokens using BODMAS
    private static double evaluateTokens(List<Token> tokens) {
        // Convert infix to postfix for easier evaluation
        List<Token> postfix = convertToPostfix(tokens);
        return evaluatePostfix(postfix);
    }

    // Step 2.2.1: Convert infix tokens to postfix (Shunting Yard Algorithm)
    private static List<Token> convertToPostfix(List<Token> tokens) {
        Deque<OperatorToken> operatorStack = new ArrayDeque<>();
        List<Token> postfix = new ArrayList<>();

        for (Token token : tokens) {
            if (token instanceof NumberToken numberToken) {
                postfix.add(numberToken);
            } else if (token instanceof OperatorToken operatorToken) {
                if (operatorToken.operator() == '(') {
                    operatorStack.push(operatorToken);
                } else if (operatorToken.operator() == ')') {
                    while (!operatorStack.isEmpty() && operatorStack.peek().operator() != '(') {
                        postfix.add(operatorStack.pop());
                    }
                    operatorStack.pop(); // Pop '('
                } else {
                    while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(operatorToken)) {
                        postfix.add(operatorStack.pop());
                    }
                    operatorStack.push(operatorToken);
                }
            }
        }
        while (!operatorStack.isEmpty()) {
            postfix.add(operatorStack.pop());
        }
        return postfix;
    }

    // Step 2.2.2: Evaluate the postfix expression
    private static double evaluatePostfix(List<Token> postfix) {
        Deque<Double> stack = new ArrayDeque<>();
        for (Token token : postfix) {
            if (token instanceof NumberToken numberToken) {
                stack.push(numberToken.value());
            } else if (token instanceof OperatorToken operatorToken) {
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(a, b, operatorToken.operator()));
            }
        }
        return stack.pop();
    }

    // Helper: Apply operator to two numbers
    private static double applyOperator(double a, double b, char operator) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }

    // Helper: Get operator precedence
    private static int precedence(OperatorToken operatorToken) {
        return switch (operatorToken.operator()) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            default -> 0;
        };
    }

    // Sealed interface for Tokens
    sealed interface Token permits NumberToken, OperatorToken {
    }

    // Record for Number tokens
    record NumberToken(double value) implements Token {
    }

    // Record for Operator tokens
    record OperatorToken(char operator) implements Token {
    }

}
```

## 3. Desired Microservice Architechture:

Now let's refine the monolithic codebase under `BODMASCalculator` above in a more microservice based architechture:
You can refer the attached flow diagram which has a highlevel view

---

1. **Generate Service**:

    - **REST Endpoint `/generate`**:
        - Generates and returns a **random infix expression** as a string (e.g., `3 + 5 * (2 - 1)`).
        - Accessible via Postman for manual testing.

2. **Evaluate Service**:

    - **gRPC Endpoint `/evaluate`**:
      This should also be Accessible over postman for manual testing
        - Accepts an infix expression as input.
        - Performs the following steps:
            1. Calls the **Tokenize Service** to convert the infix expression into a **list of POSTFIX tokens** (
               numbers, operators, brackets).
            2. Processes these tokens based on `evaluatePostfix` method from `BODMASCalculator` and calls **Calculator
               Service** over gRPC, to evaluate the final result, applying BODMAS rules.
        - Returns the calculated result.

3. **Tokenize Service**:

    - **REST based tokenization Endpoint `/tokenize`**:
        - Accepts an infix expression (e.g., `3 + 5 * (2 - 1)`).
        - Converts the infix expression into a **list of tokens** in the POSTFIX notation,
          like (`3`, `+`, `5`, `*`, `(`, `2`, `-`, `1`, `)`).
        - sends tokens back to the Evaluate Service.

4. **Calculator Service**:

    - **gRPC Endpoint with Basic Math Operations**:
        - Exposes operations like `add`, `subtract`, `multiply`, `divide`.
        - invoked as per logic in `evaluatePostfix` and `applyOperator` logic in `BODMASCalculator`

5. **Test Service**:
    - **REST Endpoint `/test`**:
        - Acts as the **integration service**:
            1. Calls the `/generate` REST endpoint to fetch a random infix expression.
            2. Sends this expression to the `/evaluate` gRPC endpoint.
            3. Returns the final result to the client.
        - Can be used to load test the complete system (`generate → tokenize → calculate`).

---

### **Revised Flow Recap**

1. **REST Endpoint** `/generate` produces the **infix expression**.
2. **REST Endpoint** `/tokenize` produces POSTFIX tokens list derived from the infix expression.
3. **gRPC Endpoint** `/evaluate` processes the token list, applies BODMAS, and calculates the result using the *
   *Calculator Service**.
4. **gRPC Endpoint** `add` `subtract` `multiply` `divide` actions exposed by **Calculator Service**
5. **REST Endpoint** `/test` integrates the flow for testing and benchmarking through load testing tools like jMeter or
   K6.

---
