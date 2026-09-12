package com.swasthyasathi.app.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 maps to host PC's localhost from Android Emulator
    // For USB physical device, run: adb reverse tcp:8000 tcp:8000 and use http://localhost:8000/
    var baseUrl: String = "http://10.0.2.2:8000/"
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            apiServiceInstance = null
        }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private var apiServiceInstance: ApiService? = null

    val apiService: ApiService
        get() {
            if (apiServiceInstance == null) {
                apiServiceInstance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return apiServiceInstance!!
        }
}
