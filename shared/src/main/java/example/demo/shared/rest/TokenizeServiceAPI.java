package example.demo.shared.rest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface TokenizeServiceAPI {
    @POST("/tokenize")
    Call<List<String>> tokenize(@Body String expression);
}