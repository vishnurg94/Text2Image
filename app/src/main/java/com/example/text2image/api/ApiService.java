package com.example.text2image.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/auth")
    Call<AuthResponse> authenticate(@Header("Authorization") String bearerToken);

    @POST("/generate_image")
    Call<String> generateImage(@Header("Authorization") String bearerToken, @Body ImageGenRequest request);
}