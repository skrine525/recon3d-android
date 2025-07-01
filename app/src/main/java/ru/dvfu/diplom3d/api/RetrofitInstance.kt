package ru.dvfu.diplom3d.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request

object RetrofitInstance {
    private val AUTH_ENDPOINTS = listOf(
        "GET" to Regex("/api/v1/users/me/"),
        "PUT" to Regex("/api/v1/users/me/"),
        "POST" to Regex("/api/v1/token/logout/"),
        "POST" to Regex("/api/v1/users/set_password/"),
        "GET" to Regex("/api/v1/users"),
        "PUT" to Regex("/api/v1/users/\\d+/"),
        "GET" to Regex("/api/v1/users/\\d+/"),
        "PUT" to Regex("/api/v1/common/users/\\d+/change-password/"),
        "PUT" to Regex("/api/v1/common/users/\\d+/change-is-active/"),
        "PUT" to Regex("/api/v1/common/users/\\d+/change-is-staff/"),
        "PUT" to Regex("/api/v1/common/users/\\d+/change-is-superuser/"),
        "POST" to Regex("/api/v1/upload/plan-photo/"),
        "POST" to Regex("/api/v1/reconstruction/initial-masks"),
        "POST" to Regex("/api/v1/upload/user-mask/"),
        "POST" to Regex("/api/v1/reconstruction/houghs"),
        "POST" to Regex("/api/v1/reconstruction/reconstructions"),
        "PUT" to Regex("/api/v1/reconstruction/reconstructions/\\d+/save"),
        "GET" to Regex("/api/v1/reconstruction/reconstructions"),
        "PATCH" to Regex("/api/v1/reconstruction/reconstructions/\\d+"),
        "DELETE" to Regex("/api/v1/reconstruction/reconstructions/\\d+"),
        "POST" to Regex("/api/v1/upload/user-environment-photo/"),
        "POST" to Regex("/api/v1/identification/identifications"),
        "PUT" to Regex("/api/v1/reconstruction/reconstructions/\\d+/rooms"),
    )

    fun getApiService(baseUrl: String, context: Context? = null): ApiService {
        val prefs = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs?.getString("auth_token", null)
        val clientBuilder = OkHttpClient.Builder()
        if (!token.isNullOrEmpty()) {
            clientBuilder.addInterceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val url = original.url.toString()
                val requestBuilder = original.newBuilder()
                val method = original.method.uppercase()
                if (AUTH_ENDPOINTS.any { (m, endpoint) -> method == m && endpoint.containsMatchIn(url) }) {
                    requestBuilder.header("Authorization", "Token $token")
                }
                val response = chain.proceed(requestBuilder.build())
                if (AUTH_ENDPOINTS.any { (m, endpoint) -> original.method.uppercase() == m && endpoint.containsMatchIn(url) } && response.code == 401 && context != null) {
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .edit().remove("auth_token").apply()
                }
                response
            }
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()
        return retrofit.create(ApiService::class.java)
    }
} 