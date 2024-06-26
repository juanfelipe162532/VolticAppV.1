package com.lhdevelopment.voltic

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @POST
    fun registerUser(@Url url: String, @Body body: RegisterRequestBody): Call<ApiResponse>
}






