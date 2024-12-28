package example.calc.tester;

import io.micrometer.core.instrument.binder.grpc.ObservationGrpcClientInterceptor;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.ExecutorService;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"example.calc.tester", "example.demo.shared"})
public class TesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TesterApplication.class, args);
    }

    @Bean
    public ObservationGrpcClientInterceptor grpcClientInterceptor(ObservationRegistry observationRegistry) {
        return new ObservationGrpcClientInterceptor(observationRegistry);
    }

    @Bean
    public GrpcChannelConfigurer channelConfigurer(
            ObservationGrpcClientInterceptor grpcClientInterceptor,
            ExecutorService executorService
    ) {
        return (channelBuilder, name) -> {
            log.info("channel builder {}", name);
            channelBuilder.intercept(grpcClientInterceptor)
                    .executor(executorService);
        };
    }

}
