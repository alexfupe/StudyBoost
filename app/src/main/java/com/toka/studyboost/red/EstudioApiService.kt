package com.toka.studyboost.red

import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Interface de Retrofit para el Backend de Study Flow.
 */
interface EstudioApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadResponse

    @GET("results/{id}")
    suspend fun getResults(@Path("id") id: String): ResultadosResponse
}
