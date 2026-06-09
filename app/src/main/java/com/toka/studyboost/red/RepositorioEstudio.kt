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
    suspend fun subirYProcesar(uri: Uri, titulo: String, contexto: Context, userId: Int): SesionEstudio?
    suspend fun subirYProcesarTexto(texto: String, titulo: String, userId: Int): SesionEstudio?
    suspend fun obtenerPreguntasDeSesion(idSesion: String): List<PreguntaTest>
    suspend fun guardarResultadoTest(idSesion: String, aciertos: Int, total: Int)
    
    // —— Autenticación —————————————————————————————————————————————————————————
    suspend fun registrarUsuario(nombre: String, email: String, contrasena: String): Usuario
    suspend fun iniciarSesion(email: String, contrasena: String): Usuario
    suspend fun cambiarContrasena(email: String, actual: String, nueva: String)
}
