package com.lhdevelopment.voltic

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @POST
    fun registerUser(@Url url: String, @Body body: RegisterRequestBody): Call<ApiResponse>

    @POST
    fun loginUser(@Url url: String, @Body requestBody: RegisterRequestBody): Call<AuthResponse>

    @POST
    fun sendRouteData(@Url url: String, @Body body: String): Call<Void>
}









