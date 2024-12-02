package example.demo.shared.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Configuration
public class RetrofitConfig {

    private final RetrofitProperties retrofitProperties;

    public RetrofitConfig(RetrofitProperties retrofitProperties) {
        this.retrofitProperties = retrofitProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules(); // Automatically supports records and Java 17 features
    }

    @Bean
    public Retrofit retrofit(ObjectMapper objectMapper, ObservationRegistry observationRegistry) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                // This setup ensures that Retrofit utilizes the OkHttpClient configured with the observation interceptor,
                // enabling tracing for all HTTP requests made through Retrofit.
                .addInterceptor(OkHttpObservationInterceptor.builder(observationRegistry, "okhttp.requests").build())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);

        if (retrofitProperties.isProxyEnabled()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                    retrofitProperties.getProxyHost(), retrofitProperties.getProxyPort()));
            clientBuilder.proxy(proxy);
        }

        clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        return new Retrofit.Builder()
                .baseUrl(retrofitProperties.getBaseUrl())
                .client(clientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create()) // to handle string response
                //.addConverterFactory(GsonConverterFactory.create()) // to handle JSON response
                .addConverterFactory(JacksonConverterFactory.create(objectMapper)) // to handle polymorphic sealed hierarchy
                .build();
    }

    @Bean
    public TokenizeAPIService tokenizeServiceAPI(Retrofit retrofit) {
        return retrofit.create(TokenizeAPIService.class);
    }

    @Bean
    public GenerateAPIService generateServiceAPI(Retrofit retrofit) {
        return retrofit.create(GenerateAPIService.class);
    }
}
