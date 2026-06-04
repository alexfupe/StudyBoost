package com.toka.studyboost.red

import com.google.gson.annotations.SerializedName

/**
 * DTOs para la comunicación con el Backend.
 */

// —— Autenticación ————————————————————————————————————————————————————————————

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val id: String,
    val nombre: String,
    val email: String,
    val token: String? = null
)

// —— Procesamiento de Documentos ———————————————————————————————————————————————

data class UploadResponse(
    val id: String,
    val message: String
)

data class ResultadosResponse(
    val summary: String,
    val questions: List<PreguntaRemota>
)

data class PreguntaRemota(
    val question: String,
    val options: List<String>,
    val correct: Int
)
