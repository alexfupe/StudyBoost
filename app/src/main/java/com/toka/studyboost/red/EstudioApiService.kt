package com.toka.studyboost.red

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Interface de Retrofit para el Backend C# (ASP.NET Core).
 */
interface EstudioApiService {

    // —— Autenticación —————————————————————————————————————————————————————————

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): User

    @POST("api/Auth/register")
    suspend fun register(@Body request: RegisterRequest): okhttp3.ResponseBody

    @POST("api/Auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): okhttp3.ResponseBody

    // —— Documentos (PDFs) —————————————————————————————————————————————————————

    @Multipart
    @POST("api/Documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("userId") userId: RequestBody
    ): Document

    @POST("api/Documents/upload-text")
    suspend fun uploadText(@Body request: UploadTextRequest): Document

    @GET("api/Documents/user/{userId}")
    suspend fun listUserDocuments(@Path("userId") userId: Int): List<Document>

    @GET("api/Documents/{id}")
    suspend fun getDocument(@Path("id") id: Int): Document

    @GET("api/Documents/{id}/questions")
    suspend fun generateQuestions(
        @Path("id") id: Int,
        @Query("type") type: String = "mixed"
    ): okhttp3.ResponseBody
}
