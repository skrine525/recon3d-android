package ru.dvfu.diplom3d.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private var retrofit: Retrofit? = null
    private var lastBaseUrl: String? = null

    fun getApiService(baseUrl: String): ApiService {
        if (retrofit == null || lastBaseUrl != baseUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            lastBaseUrl = baseUrl
        }
        return retrofit!!.create(ApiService::class.java)
    }
} 