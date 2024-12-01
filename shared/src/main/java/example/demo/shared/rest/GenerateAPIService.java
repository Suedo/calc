package example.demo.shared.rest;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GenerateAPIService {
    @GET("/generate")
    Call<String> generateExpression();
}