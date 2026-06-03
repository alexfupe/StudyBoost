package com.toka.studyboost.red

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.gson.Gson
import com.toka.studyboost.datos.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

private const val TAG = "RepositorioEstudio"

/**
 * Repositorio central de StudyBoost.
 *
 * Sigue el patrón "Single Source of Truth":
 * - La base de datos Room es la fuente de verdad.
 * - La red (Gemini) solo se usa cuando no hay datos en caché.
 * - Los ViewModels solo interactúan con este repositorio, nunca directamente
 *   con la base de datos o la red.
 *
 * @param db        Base de datos Room (provista por [MainApplication]).
 * @param gemini    Servicio de la API de Gemini.
 */
class RepositorioEstudio(
    private val db: StudyBoostDatabase,
    private val gemini: GeminiService = GeminiService()
) {
    private val gson = Gson()

    // —— Sesiones de estudio (historial) ————————————————————————————————————————

    /**
     * Flujo reactivo de todas las sesiones guardadas, ordenadas por fecha.
     * La UI se actualiza automáticamente cuando se insertan/eliminan sesiones.
     */
    fun observarSesiones(): Flow<List<SesionEstudio>> =
        db.sesionEstudioDao().observarTodas()

    suspend fun eliminarSesion(sesion: SesionEstudio) {
        db.sesionEstudioDao().eliminar(sesion)
    }

    // —— Procesamiento de documentos con IA ————————————————————————————————————

    /**
     * Procesa un texto con la IA y guarda TODO en Room automáticamente:
     * la sesión, las preguntas y las flashcards generadas.
     *
     * @param texto  Texto extraído del PDF/imagen (ya truncado por el ViewModel).
     * @param titulo Nombre del archivo, para mostrar en el historial.
     * @return La [SesionEstudio] creada, o `null` si la IA no responde.
     */
    suspend fun procesarYGuardar(texto: String, titulo: String): SesionEstudio? {
        Log.d(TAG, "Procesando documento: $titulo")

        // 1. Llamar a la IA para resumen + preguntas
        val respuesta = gemini.analizarDocumento(texto) ?: return null

        // 2. Crear y guardar la sesión
        val sesionId = java.util.UUID.randomUUID().toString()
        val sesion = SesionEstudio(
            id = sesionId,
            titulo = titulo,
            resumen = respuesta.summary,
            totalPreguntas = respuesta.questions.size
        )
        db.sesionEstudioDao().insertar(sesion)

        // 3. Guardar las preguntas en Room
        val preguntasRoom = respuesta.questions.map { q ->
            PreguntaGuardada(
                idSesion = sesionId,
                enunciado = q.question,
                opcionesJson = gson.toJson(q.options),
                respuestaCorrecta = q.correct
            )
        }
        db.preguntaGuardadaDao().insertarTodas(preguntasRoom)

        // 4. Generar flashcards en paralelo (no bloquea el flujo principal)
        try {
            val flashcardsRaw = gemini.generarFlashcards(texto)
            if (flashcardsRaw != null) {
                val flashcards = flashcardsRaw.flashcards.map { fc ->
                    Flashcard(
                        idSesion = sesionId,
                        pregunta = fc.pregunta,
                        respuesta = fc.respuesta
                    )
                }
                db.flashcardDao().insertarTodas(flashcards)
                Log.d(TAG, "${flashcards.size} flashcards generadas y guardadas")
            }
        } catch (e: Exception) {
            // Las flashcards son opcionales — no bloquear el flujo si fallan
            Log.e(TAG, "Error generando flashcards (no crítico): ${e.message}")
        }

        Log.d(TAG, "Sesión guardada con id=$sesionId")
        return sesion
    }

    /**
     * Obtiene las preguntas de una sesión para mostrarlas en la UI.
     * Las convierte de [PreguntaGuardada] (Room) a [PreguntaTest] (modelo de UI).
     */
    suspend fun obtenerPreguntasDeSesion(idSesion: String): List<PreguntaTest> {
        return db.preguntaGuardadaDao()
            .observarPorSesion(idSesion)
            .first()
            .map { p ->
                val opciones: List<String> = try {
                    gson.fromJson(p.opcionesJson, Array<String>::class.java).toList()
                } catch (e: Exception) { emptyList() }
                PreguntaTest(p.id.toString(), p.enunciado, opciones, p.respuestaCorrecta)
            }
    }

    // —— Flashcards ————————————————————————————————————————————————————————————

    /** Flujo de flashcards pendientes de revisar hoy (SM-2). */
    fun observarFlashcardsParaHoy(): Flow<List<Flashcard>> =
        db.flashcardDao().observarParaRevisar()

    fun observarFlashcardsDeSesion(idSesion: String): Flow<List<Flashcard>> =
        db.flashcardDao().observarPorSesion(idSesion)

    suspend fun actualizarFlashcard(flashcard: Flashcard) {
        db.flashcardDao().guardarOActualizar(flashcard)
    }

    /** Número de flashcards pendientes hoy, para el badge de la pantalla principal. */
    fun observarContadorFlashcardsHoy(): Flow<Int> =
        db.flashcardDao().contarParaHoy()

    // —— Utilidades ————————————————————————————————————————————————————————————

    /** Comprueba si hay conexión a internet activa. */
    fun hayConexion(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork?.let { red ->
            cm.getNetworkCapabilities(red)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    }

    // —— Métodos legacy de simulación (aún usados por pantallas de auth) ———————

    suspend fun registrarUsuario(nombre: String, email: String, contrasena: String): Usuario {
        kotlinx.coroutines.delay(1500)
        return Usuario("1", nombre, email)
    }

    suspend fun iniciarSesion(email: String, contrasena: String): Usuario {
        kotlinx.coroutines.delay(1500)
        return Usuario("1", "Usuario de Prueba", email)
    }

    suspend fun cambiarContrasena(actual: String, nueva: String) {
        kotlinx.coroutines.delay(2000)
    }
}
