package com.toka.studyboost.red

import android.content.Context
import android.net.Uri
import com.toka.studyboost.datos.*
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del Repositorio para permitir Mocks y desacoplamiento.
 */
interface RepositorioEstudio {
    // —— Sesiones ——————————————————————————————————————————————————————————————
    fun observarSesiones(): Flow<List<SesionEstudio>>
    suspend fun eliminarSesion(sesion: SesionEstudio)
    
    // —— Procesamiento —————————————————————————————————————————————————————————
    /** Sube un archivo y devuelve la sesión creada. */
    suspend fun subirYProcesar(uri: Uri, titulo: String, contexto: Context): SesionEstudio?
    suspend fun obtenerPreguntasDeSesion(idSesion: String): List<PreguntaTest>
    
    // —— Flashcards ————————————————————————————————————————————————————————————
    fun observarFlashcardsParaHoy(): Flow<List<Flashcard>>
    fun observarFlashcardsDeSesion(idSesion: String): Flow<List<Flashcard>>
    suspend fun actualizarFlashcard(flashcard: Flashcard)
    fun observarContadorFlashcardsHoy(): Flow<Int>
    
    // —— Autenticación —————————————————————————————————————————————————————————
    suspend fun registrarUsuario(nombre: String, email: String, contrasena: String): Usuario
    suspend fun iniciarSesion(email: String, contrasena: String): Usuario
    suspend fun cambiarContrasena(actual: String, nueva: String)
}
