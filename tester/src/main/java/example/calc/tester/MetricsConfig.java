package example.calc.tester;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    // todo: check? why is this explicitly needed here as opposed to Generator which exposes these automatically ??
    @Bean
    public ProcessorMetrics processorMetrics(MeterRegistry registry) {
        final ProcessorMetrics processorMetrics = new ProcessorMetrics();
        final JvmMemoryMetrics jvmMemoryMetrics = new JvmMemoryMetrics();
        processorMetrics.bindTo(registry);
        jvmMemoryMetrics.bindTo(registry);
        return processorMetrics;
    }
}
