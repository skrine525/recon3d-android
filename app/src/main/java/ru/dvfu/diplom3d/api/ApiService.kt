package ru.dvfu.diplom3d.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Path
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.DELETE
import retrofit2.http.PATCH

interface ApiService {
    @GET("/api/v1/common/info")
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

    @Multipart
    @POST("/api/v1/upload/plan-photo/")
    suspend fun uploadPlanPhoto(
        @Part file: MultipartBody.Part
    ): Response<UploadPhotoResponse>

    @POST("/api/v1/reconstruction/initial-masks")
    @Headers("Content-Type: application/json")
    suspend fun calculateInitialMask(
        @Body body: CalculateMaskRequest
    ): Response<CalculateMaskResponse>

    @Multipart
    @POST("/api/v1/upload/user-mask/")
    suspend fun uploadUserMask(
        @Part file: MultipartBody.Part
    ): Response<UploadPhotoResponse>

    @POST("/api/v1/reconstruction/houghs")
    @Headers("Content-Type: application/json")
    suspend fun calculateHough(
        @Body body: CalculateHoughRequest
    ): Response<CalculateHoughResponse>

    @POST("/api/v1/reconstruction/reconstructions")
    @Headers("Content-Type: application/json")
    suspend fun calculateMesh(
        @Body body: CalculateMeshRequest
    ): Response<CalculateMeshResponse>

    @PUT("/api/v1/reconstruction/reconstructions/{id}/save")
    suspend fun saveReconstruction(@Path("id") id: Int, @Body body: SaveReconstructionRequest): Response<CalculateMeshResponse>

    @GET("/api/v1/reconstruction/reconstructions")
    suspend fun getReconstructions(
        @Query("is_saved") isSaved: Int = 1
    ): Response<List<ReconstructionListItem>>

    @GET("/api/v1/reconstruction/reconstructions/{id}")
    suspend fun getReconstructionById(@Path("id") id: Int): Response<CalculateMeshResponse>

    @DELETE("/api/v1/reconstruction/reconstructions/{id}")
    suspend fun deleteReconstruction(@Path("id") id: Int): Response<Void>

    @PATCH("/api/v1/reconstruction/reconstructions/{id}")
    suspend fun patchReconstruction(@Path("id") id: Int, @Body body: PatchReconstructionRequest): Response<Void>

    @Multipart
    @POST("/api/v1/upload/user-environment-photo/")
    suspend fun uploadUserEnvironmentPhoto(
        @Part file: MultipartBody.Part
    ): Response<UploadPhotoResponse>

    @POST("/api/v1/identification/identifications")
    suspend fun identification(@Body body: IdentificationRequest): Response<IdentificationResponse>
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

data class UploadPhotoResponse(
    val id: String,
    val url: String,
    val file_type: Int,
    val source_type: Int,
    val uploaded_by: Int,
    val uploaded_at: String
)

data class CalculateMaskRequest(val file_id: String)
data class CalculateMaskResponse(
    val id: String,
    val source_upload_file_id: String,
    val created_at: String,
    val created_by: Int,
    val url: String
)

data class CalculateHoughRequest(
    val plan_file_id: String,
    val user_mask_file_id: String
)
data class CalculateHoughResponse(
    val id: String,
    val plan_upload_file_id: String,
    val user_mask_upload_file_id: String,
    val created_at: String,
    val created_by: Int,
    val url: String
)

data class CalculateMeshRequest(
    val plan_file_id: String,
    val user_mask_file_id: String
)
data class CalculateMeshResponse(
    val id: Int,
    val name: String,
    val status: Int,
    val status_display: String,
    val created_at: String,
    val created_by: Int,
    val saved_at: String?,
    val url: String?
)

data class SaveReconstructionRequest(val name: String)

data class ReconstructionListItem(
    val id: Int,
    val name: String
)

data class PatchReconstructionRequest(val name: String)

data class IdentificationRequest(
    val reconstruction_id: Int,
    val file_id: String,
    val scale: Int
)
data class IdentificationResponse(
    val x: Double,
    val y: Double,
    val angle: Double
) 