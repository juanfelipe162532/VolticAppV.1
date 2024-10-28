package com.lhdevelopment.voltic

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class AuthResponse(
    val statusCode: Int,
    val headers: Map<String, String>, // O usa un tipo más específico si lo prefieres
    val body: String // Asegúrate de que sea un String para poder parsearlo como JSON
)


