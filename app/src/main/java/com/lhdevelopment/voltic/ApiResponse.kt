package com.lhdevelopment.voltic

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class AuthResponse(
    val statusCode: Int,
    val message: String,
    val ID_Usuario: Int
)


