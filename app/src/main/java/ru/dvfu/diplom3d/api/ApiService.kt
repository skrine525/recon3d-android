package ru.dvfu.diplom3d.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/v1/common/meta")
    suspend fun checkServer(): Response<Void>

    @POST("/api/v1/token/login/")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("/api/v1/users/")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @GET("/api/v1/users/me/")
    suspend fun getMe(): Response<UserMeResponse>

    @POST("/api/v1/token/logout/")
    suspend fun logout(): Response<Void>
}

// Примеры моделей для login/register

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val auth_token: String)

data class RegisterRequest(val username: String, val password: String, val re_password: String)
data class RegisterResponse(val email: String, val username: String, val id: Int)

data class UserMeResponse(val id: Int, val username: String, val is_staff: Boolean) 