## Micrometer based manual context propagation:

https://docs.micrometer.io/context-propagation/reference/usage.html

In your `testFlow` method, you are encountering two problems:

1. **Tracing context (e.g., MDC values) is missing** in logs from tasks executed on virtual threads.
2. Virtual threads created for `generateExpression` are not associated with the `Tomcat` request thread, leading to
   missing context.

The **Micrometer Context Propagation library** can address these issues by ensuring that the `ThreadLocal` context (such
as MDC for tracing) is propagated to the virtual threads used in `executor.submit()`.

Here’s how you can fix your code using Micrometer Context Propagation:

---

### Updated Code with Micrometer Context Propagation

#### 1. Add Dependencies

Include the Micrometer Context Propagation library in your project:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>context-propagation</artifactId>
    <version>1.1.2</version>
</dependency>
```

---

#### 2. Register a Context Registry

Register a `ContextRegistry` with a `ThreadLocalAccessor` to enable context propagation:

```java
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;
import io.micrometer.context.ContextSnapshot;

public class MDCThreadLocalAccessor implements ThreadLocalAccessor<String> {

    public static final String KEY = "traceId";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public String getValue() {
        return MDC.get(KEY); // Retrieve the MDC value
    }

    @Override
    public void setValue(String value) {
        MDC.put(KEY, value); // Set the MDC value
    }

    @Override
    public void setValue() {
        MDC.clear(); // Clear the MDC value
    }
}

// In your application's setup (e.g., main method or @Configuration class):
ContextRegistry.getInstance().registerThreadLocalAccessor(new MDCThreadLocalAccessor());
```

This ensures the `traceId` (or any other MDC value) is correctly propagated across thread boundaries.

---

#### 3. Wrap Tasks in `ContextSnapshot`

Modify the `testFlow` method to capture the current context and propagate it to virtual threads:

```java
public String testFlow() {
    try {
        log.info("testFlow: Current Thread: {}", Thread.currentThread());
        final Instant start = Instant.now();

        // Step 1: Capture context and wrap tasks
        ContextSnapshot snapshot = ContextSnapshot.captureAll();

        var expressionF = this.executor.submit(snapshot.wrap(this::generateExpression));
        var dummy1F = this.executor.submit(snapshot.wrap(this::generateExpression));
        var dummy2F = this.executor.submit(snapshot.wrap(this::generateExpression));

        // Step 2: Evaluate the expression via gRPC
        String expression = expressionF.get();
        log.info("testFlow: expression obtained: {}, Thread: {}", expression, Thread.currentThread());

        Evaluate.EvaluateRequest request = Evaluate.EvaluateRequest.newBuilder()
                .setExpression(expression)
                .build();

        var result = evaluateServiceClient.evaluate(request).getResult();

        final String resultString = String.format(
                "generated {%s} and evaluated its value as {%.8f} in %d ms",
                expression, result, Duration.between(start, Instant.now()).toMillis()
        );
        log.info(resultString);
        return resultString;
    } catch (Exception e) {
        log.error("error", e);
        throw new RuntimeException(e);
    }
}
```

---

#### 4. Ensure Tracing Context in `generateExpression`

Your `generateExpression` method remains unchanged because the context will now be available when the task runs:

```java
private String generateExpression() {
    log.info("generateExpression: Current Thread: {}", Thread.currentThread());
    return restClient
            .get()
            .uri("/generate")
            .retrieve()
            .body(String.class);
}
```

---

### How This Solves Your Issues

1. **Tracing Context Propagation:**
    - The `ContextSnapshot.wrap()` ensures that the `traceId` (or any `ThreadLocal` value) is carried over to the
      virtual threads executing `generateExpression` and `dummy` tasks.
    - This resolves the missing tracing
      context (`[     virtual-78] [                                                 ]`) in the logs.

2. **Thread Name Consistency:**
    - Virtual threads created by `executor.submit()` are not tied to Tomcat. However, with the context propagation, your
      MDC (and other context) will now appear in the logs, making it easier to correlate logs even if the thread names
      differ.

---

With this implementation, your logs should now include the `traceId` (or any other MDC information), even for tasks
executed on virtual threads.

### PS: Code Snippets I did:

In Main application file:

```java
    @Bean
    public ContextRegistry contextRegistry() {
        final MDCThreadLocalAccessor mdcThreadLocalAccessor = new MDCThreadLocalAccessor();
        final ContextRegistry contextRegistry = new ContextRegistry();
        contextRegistry.registerThreadLocalAccessor(mdcThreadLocalAccessor);
        return contextRegistry;
    }
```

In the TestService file:

```java
            ContextSnapshot snapshot = ContextSnapshotFactory.builder().contextRegistry(contextRegistry).build().captureAll();

var expressionF = this.executor.submit(snapshot.wrap(this::generateExpression));
var dummy1F = this.executor.submit(snapshot.wrap(this::generateExpression));
var dummy2F = this.executor.submit(snapshot.wrap(this::generateExpression));
```