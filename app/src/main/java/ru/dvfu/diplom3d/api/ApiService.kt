package ru.dvfu.diplom3d.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/v1/common/meta")
    suspend fun checkServer(): Response<Void>

    @POST("/api/v1/common/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("/api/v1/common/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>
}

// Примеры моделей для login/register

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

data class RegisterRequest(val username: String, val password: String)
data class RegisterResponse(val success: Boolean) 