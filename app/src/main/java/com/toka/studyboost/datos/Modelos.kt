package com.toka.studyboost.datos

import androidx.compose.runtime.Immutable

@Immutable
data class Usuario(
    val id: String,
    val nombre: String,
    val email: String
)

@Immutable
data class PeticionRegistro(
    val nombre: String,
    val email: String,
    val contrasena: String
)

@Immutable
data class PeticionInicioSesion(
    val email: String,
    val contrasena: String
)

@Immutable
data class Apunte(
    val id: String,
    val titulo: String,
    val fecha: String,
    val contenido: String
)

@Immutable
data class Resumen(
    val idApunte: String,
    val texto: String
)

@Immutable
data class PreguntaTest(
    val id: String,
    val enunciado: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int
)

@Immutable
data class Estadisticas(
    val totalDocumentos: Int,
    val promedioAciertos: Float,
    val tiempoEstudioHoras: Float
)

// Modelos para la API de Gemini
data class GeminiResponse(
    val summary: String,
    val questions: List<GeminiQuestion>
)

data class GeminiQuestion(
    val question: String,
    val options: List<String>,
    val correct: Int
)
