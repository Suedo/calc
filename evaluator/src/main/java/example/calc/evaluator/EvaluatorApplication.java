package example.calc.evaluator;

import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import io.micrometer.core.instrument.binder.grpc.ObservationGrpcServerInterceptor;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.ExecutorService;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"example.calc.evaluator", "example.demo.shared"})
public class EvaluatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvaluatorApplication.class, args);
    }

    @Bean
    public ObservationGrpcServerInterceptor grpcServerInterceptor(ObservationRegistry observationRegistry) {
        return new ObservationGrpcServerInterceptor(observationRegistry);
    }

    @Bean
    public ObservationGrpcClientInterceptor grpcClientInterceptor(ObservationRegistry observationRegistry) {
        return new ObservationGrpcClientInterceptor(observationRegistry);
    }

    @Bean
    public GrpcChannelConfigurer channelConfigurer(
            ObservationGrpcClientInterceptor grpcClientInterceptor,
            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutorService
    ) {
        return (channelBuilder, name) -> {
            log.info("channel builder {}", name);
            channelBuilder.intercept(grpcClientInterceptor)
                    .executor(virtualThreadExecutorService);
        };
    }

}
