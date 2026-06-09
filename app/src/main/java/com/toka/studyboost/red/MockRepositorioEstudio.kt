package com.toka.studyboost.red

import android.content.Context
import android.net.Uri
import com.toka.studyboost.datos.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

/**
 * Implementación del repositorio que conecta con la API externa (Retrofit)
 * y la base de datos local (Room). Gestiona la persistencia de sesiones y preguntas.
 */
class MockRepositorioEstudio(
    private val db: StudyBoostDatabase
) : RepositorioEstudio {

    override fun observarSesiones(): Flow<List<SesionEstudio>> =
        db.sesionEstudioDao().observarTodas()

    override suspend fun eliminarSesion(sesion: SesionEstudio) {
        db.sesionEstudioDao().eliminar(sesion)
    }

    override suspend fun subirYProcesar(uri: Uri, titulo: String, contexto: Context, userId: Int): SesionEstudio? {
        // Leemos el archivo del Uri
        val inputStream = contexto.contentResolver.openInputStream(uri) ?: return null
        val tempFile = java.io.File.createTempFile("upload", ".pdf", contexto.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        val requestFile = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
        val body = okhttp3.MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
        val userBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        // Subimos el documento a la API
        val document = RetrofitClient.instance.uploadDocument(body, userBody)
        tempFile.delete()

        return procesarDocumento(document, titulo)
    }

    override suspend fun subirYProcesarTexto(texto: String, titulo: String, userId: Int): SesionEstudio? {
        val request = UploadTextRequest(userId, titulo, texto)
        val document = RetrofitClient.instance.uploadText(request)
        return procesarDocumento(document, titulo)
    }

    private suspend fun procesarDocumento(document: Document, titulo: String): SesionEstudio {
        // Pedimos las preguntas a la API
        val responseBody = RetrofitClient.instance.generateQuestions(document.id)
        val rawQuestions = responseBody.string()
        
        // El API devuelve un JSON string con las preguntas, lo parseamos
        val typeToken = object : com.google.gson.reflect.TypeToken<List<PreguntaRemota>>() {}.type
        val mockPreguntas: List<PreguntaRemota> = try {
            com.google.gson.Gson().fromJson(rawQuestions, typeToken) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val sesion = SesionEstudio(
            id = document.id.toString(),
            titulo = titulo,
            resumen = document.summary ?: "",
            totalPreguntas = mockPreguntas.size
        )
        
        val preguntasRoom = mockPreguntas.map { q ->
            PreguntaGuardada(
                idSesion = sesion.id,
                enunciado = q.question,
                opcionesJson = com.google.gson.Gson().toJson(q.options),
                respuestaCorrecta = q.correct
            )
        }

        // Insertar todo en una transacción implícita (Room lo maneja)
        db.sesionEstudioDao().insertar(sesion)
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

    override suspend fun guardarResultadoTest(idSesion: String, aciertos: Int, total: Int) {
        val sesion = db.sesionEstudioDao().obtenerPorId(idSesion)
        if (sesion != null) {
            val actualizada = sesion.copy(
                ultimoAcierto = aciertos,
                ultimoTotal = total,
                fechaUltimoTest = System.currentTimeMillis()
            )
            db.sesionEstudioDao().insertar(actualizada)
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

    override suspend fun registrarUsuario(nombre: String, email: String, contrasena: String): com.toka.studyboost.datos.Usuario {
        val reqRegistro = RegisterRequest(nombre, email, contrasena)
        RetrofitClient.instance.register(reqRegistro)
        
        val reqLogin = LoginRequest(email, contrasena)
        val apiUser = RetrofitClient.instance.login(reqLogin)
        return com.toka.studyboost.datos.Usuario(apiUser.id.toString(), apiUser.name, apiUser.email)
    }

    override suspend fun iniciarSesion(email: String, contrasena: String): com.toka.studyboost.datos.Usuario {
        val request = LoginRequest(email, contrasena)
        val apiUser = RetrofitClient.instance.login(request)
        return com.toka.studyboost.datos.Usuario(apiUser.id.toString(), apiUser.name, apiUser.email)
    }

    override suspend fun cambiarContrasena(email: String, actual: String, nueva: String) {
        val req = ChangePasswordRequest(email, actual, nueva)
        RetrofitClient.instance.changePassword(req)
    }
}
