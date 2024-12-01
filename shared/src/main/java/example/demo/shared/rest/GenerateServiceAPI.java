package example.demo.shared.rest;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GenerateServiceAPI {
    @GET("/generate")
    Call<String> generateExpression();
}