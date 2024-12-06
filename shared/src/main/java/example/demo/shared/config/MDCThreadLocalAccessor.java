package example.demo.shared.config;

import io.micrometer.context.ThreadLocalAccessor;
import org.slf4j.MDC;

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