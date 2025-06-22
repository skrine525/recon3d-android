package ru.dvfu.diplom3d.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Path

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
    suspend fun getMe(): Response<UserResponse>

    @POST("/api/v1/token/logout/")
    suspend fun logout(): Response<Void>

    @PUT("/api/v1/users/me/")
    suspend fun updateMe(@Body body: UpdateMeRequest): Response<UserResponse>

    @POST("/api/v1/users/set_password/")
    suspend fun setPassword(@Body body: SetPasswordRequest): Response<Void>

    @GET("/api/v1/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("/api/v1/users/{id}/")
    suspend fun getUser(@Path("id") id: Int): Response<UserResponse>

    @PUT("/api/v1/users/{id}/")
    suspend fun updateUser(@Path("id") id: Int, @Body body: UpdateUserRequest): Response<UserResponse>

    @PUT("/api/v1/common/users/{id}/change-password/")
    suspend fun changePassword(@Path("id") id: Int, @Body body: ChangePasswordRequest): Response<Void>

    @PUT("/api/v1/common/users/{id}/change-is-active/")
    suspend fun changeIsActive(@Path("id") id: Int, @Body body: UpdateFlagRequest): Response<Void>

    @PUT("/api/v1/common/users/{id}/change-is-staff/")
    suspend fun changeIsStaff(@Path("id") id: Int, @Body body: UpdateFlagRequest): Response<Void>

    @PUT("/api/v1/common/users/{id}/change-is-superuser/")
    suspend fun changeIsSuperuser(@Path("id") id: Int, @Body body: UpdateFlagRequest): Response<Void>
}

// Примеры моделей для login/register

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val auth_token: String)

data class RegisterRequest(val username: String, val password: String, val re_password: String)
data class RegisterResponse(val email: String, val username: String, val id: Int)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val is_staff: Boolean,
    val display_name: String,
    val is_superuser: Boolean,
    val date_joined: String,
    val is_active: Boolean
)

data class UpdateMeRequest(
    val first_name: String,
    val last_name: String,
    val email: String
)

data class SetPasswordRequest(
    val current_password: String,
    val new_password: String
)

data class UpdateUserRequest(
    val email: String,
    val first_name: String,
    val last_name: String
)

data class ChangePasswordRequest(
    val new_password: String,
    val re_new_password: String
)

data class UpdateFlagRequest(val value: Boolean) 