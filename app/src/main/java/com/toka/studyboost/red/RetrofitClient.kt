package com.toka.studyboost.red

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente central de Retrofit con configuración para ngrok.
 */
object RetrofitClient {

    private const val BASE_URL = "https://neoma-noncuspidated-tiesha.ngrok-free.dev/"

    private val ngrokInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithHeader = originalRequest.newBuilder()
            .header("ngrok-skip-browser-warning", "true")
            .build()
        chain.proceed(requestWithHeader)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
        .readTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
        .writeTimeout(15, java.util.concurrent.TimeUnit.MINUTES)
        .addInterceptor(ngrokInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: EstudioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EstudioApiService::class.java)
    }
}
