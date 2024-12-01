package example.demo.shared.rest;

import example.demo.shared.domain.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface TokenizeAPIService {
    @POST("/tokenize")
    Call<List<Token>> tokenize(@Body String expression);
}