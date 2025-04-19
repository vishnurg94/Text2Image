package com.example.text2image.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST(".")  // Using "." because the full endpoint is already specified in the BASE_URL
    Call<ImageResponse> generateImage(
            @Header("Authorization") String apiKey,
            @Body ImageRequest request
    );
} 