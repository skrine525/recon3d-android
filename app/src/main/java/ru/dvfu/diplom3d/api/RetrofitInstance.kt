package ru.dvfu.diplom3d.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request

object RetrofitInstance {
    private val AUTH_ENDPOINTS = listOf(
        "/api/v1/users/me/",
        "/api/v1/token/logout/"
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
                if (AUTH_ENDPOINTS.any { url.contains(it) }) {
                    requestBuilder.header("Authorization", "Token $token")
                }
                chain.proceed(requestBuilder.build())
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