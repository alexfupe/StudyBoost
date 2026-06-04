package com.toka.studyboost.red

import android.content.Context
import android.net.Uri
import com.toka.studyboost.datos.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

/**
 * Implementación Fake para pruebas y desarrollo paralelo.
 */
class MockRepositorioEstudio(
    private val db: StudyBoostDatabase
) : RepositorioEstudio {

    override fun observarSesiones(): Flow<List<SesionEstudio>> =
        db.sesionEstudioDao().observarTodas()

    override suspend fun eliminarSesion(sesion: SesionEstudio) {
        db.sesionEstudioDao().eliminar(sesion)
    }

    override suspend fun subirYProcesar(uri: Uri, titulo: String, contexto: Context): SesionEstudio? {
        // Simular red
        delay(3000)

        val sesionId = UUID.randomUUID().toString()
        
        // Mock de respuesta del backend
        val mockResumen = "Este es un resumen generado por el servidor a partir del archivo $titulo. " +
                "El contenido principal trata sobre los fundamentos de la arquitectura cliente-servidor y el uso de Mocks."
        
        val mockPreguntas = listOf(
            PreguntaRemota(
                "¿Cuál es el beneficio de usar Mocks?",
                listOf("Desarrollo paralelo", "Consumo de batería", "No sirven para nada"),
                0
            ),
            PreguntaRemota(
                "¿Qué método HTTP se usa para subir archivos?",
                listOf("GET", "POST Multipart", "DELETE"),
                1
            )
        )

        // Guardar en la DB local (Room) para que la UI lo vea
        val sesion = SesionEstudio(
            id = sesionId,
            titulo = titulo,
            resumen = mockResumen,
            totalPreguntas = mockPreguntas.size
        )
        db.sesionEstudioDao().insertar(sesion)

        val preguntasRoom = mockPreguntas.map { q ->
            PreguntaGuardada(
                idSesion = sesionId,
                enunciado = q.question,
                opcionesJson = com.google.gson.Gson().toJson(q.options),
                respuestaCorrecta = q.correct
            )
        }
        db.preguntaGuardadaDao().insertarTodas(preguntasRoom)

        return sesion
    }

    override suspend fun obtenerPreguntasDeSesion(idSesion: String): List<PreguntaTest> {
        return db.preguntaGuardadaDao()
            .observarPorSesion(idSesion)
            .first()
            .map { p ->
                val opciones: List<String> = try {
                    com.google.gson.Gson().fromJson(p.opcionesJson, Array<String>::class.java).toList()
                } catch (e: Exception) { emptyList() }
                PreguntaTest(p.id.toString(), p.enunciado, opciones, p.respuestaCorrecta)
            }
    }

    override fun observarFlashcardsParaHoy(): Flow<List<Flashcard>> =
        db.flashcardDao().observarParaRevisar()

    override fun observarFlashcardsDeSesion(idSesion: String): Flow<List<Flashcard>> =
        db.flashcardDao().observarPorSesion(idSesion)

    override suspend fun actualizarFlashcard(flashcard: Flashcard) {
        db.flashcardDao().guardarOActualizar(flashcard)
    }

    override fun observarContadorFlashcardsHoy(): Flow<Int> =
        db.flashcardDao().contarParaHoy()

    override suspend fun registrarUsuario(nombre: String, email: String, contrasena: String): Usuario {
        delay(1500)
        return Usuario("1", nombre, email)
    }

    override suspend fun iniciarSesion(email: String, contrasena: String): Usuario {
        delay(1500)
        return Usuario("1", "Usuario de Prueba", email)
    }

    override suspend fun cambiarContrasena(actual: String, nueva: String) {
        delay(2000)
    }
}
