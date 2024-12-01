package example.demo.shared.rest;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
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
    public Retrofit retrofit() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
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
                .addConverterFactory(GsonConverterFactory.create()) // to handle JSON response
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
