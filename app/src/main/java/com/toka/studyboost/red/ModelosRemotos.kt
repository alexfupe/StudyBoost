package com.toka.studyboost.red

/**
 * DTOs para la comunicación con el Backend ASP.NET Core.
 */

// —— Autenticación ————————————————————————————————————————————————————————————

data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class ChangePasswordRequest(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)

// —— Documentos (PDFs) ————————————————————————————————————————————————————————

data class Document(
    val id: Int,
    val userId: Int,
    val fileName: String,
    val filePath: String?,
    val extractedText: String?,
    val summary: String?,
    val status: String?,
    val uploadDate: String?
)

data class UploadTextRequest(
    val userId: Int,
    val fileName: String,
    val text: String
)

/**
 * Modelo auxiliar para el Mock y el endpoint de preguntas.
 */
data class PreguntaRemota(
    val question: String,
    val options: List<String>,
    val correct: Int
)
