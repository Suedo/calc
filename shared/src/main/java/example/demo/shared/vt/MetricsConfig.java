package example.demo.shared.vt;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class MetricsConfig {

    @Bean("virtualThreadExecutor")
    public ExecutorService taskExecutor(MeterRegistry registry) {
        log.info("Creating Virtual Thread Executor");
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        ExecutorServiceMetrics.monitor(registry, executorService, "custom.executor");
        return executorService;
    }


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
