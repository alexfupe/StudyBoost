package com.toka.studyboost.funciones_pantallas

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toka.studyboost.MainApplication
import com.toka.studyboost.datos.Flashcard
import com.toka.studyboost.datos.PreguntaTest
import com.toka.studyboost.datos.Resumen
import com.toka.studyboost.datos.SesionEstudio
import com.toka.studyboost.red.RepositorioEstudio
import com.toka.studyboost.red.MockRepositorioEstudio
import com.toka.studyboost.utils.AlgoritmoSM2
import com.toka.studyboost.utils.EscaneadorOCR
import com.toka.studyboost.utils.ExportadorContenido
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "EstudioViewModel"

/**
 * ViewModel central de la funcionalidad de estudio.
 */
class Estudio(application: Application) : AndroidViewModel(application) {

    private val app = application as MainApplication
    // Usando Mock para desarrollo
    private val repositorio: RepositorioEstudio = MockRepositorioEstudio(app.database)

    // —— Estado de la UI —————————————————————————————————————————————————————————

    var resumenActual by mutableStateOf<Resumen?>(null)
    var preguntasTest by mutableStateOf<List<PreguntaTest>>(emptyList())
    var respuestasUsuario by mutableStateOf<Map<Int, Int>>(emptyMap())
    var sesionActual by mutableStateOf<SesionEstudio?>(null)

    var progresoSubida by mutableStateOf(0f)
    var subiendo by mutableStateOf(false)
    var cargandoResultados by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    var uriArchivoSeleccionado by mutableStateOf<Uri?>(null)
    var nombreArchivoSeleccionado by mutableStateOf<String?>(null)

    // —— Flujos reactivos ——————————————————————————————————————————————————————

    val sesiones = repositorio.observarSesiones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val flashcardsParaHoyCount = repositorio.observarContadorFlashcardsHoy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // —— Selección de archivo ——————————————————————————————————————————————————————

    fun seleccionarArchivo(uri: Uri?, nombre: String?) {
        uriArchivoSeleccionado = uri
        nombreArchivoSeleccionado = nombre
        error = null
    }

    fun limpiarSeleccion() {
        uriArchivoSeleccionado = null
        nombreArchivoSeleccionado = null
        error = null
    }

    // —— Subida y procesamiento ————————————————————————————————————————————————

    private val sesion = com.toka.studyboost.red.SesionUsuario(application)

    fun subirApuntes(contexto: android.content.Context, alTerminar: (String) -> Unit) {
        val uri = uriArchivoSeleccionado ?: return

        viewModelScope.launch {
            subiendo = true
            progresoSubida = 0.1f
            error = null

            try {
                progresoSubida = 0.2f
                val titulo = nombreArchivoSeleccionado ?: "Documento sin título"

                val idStr = sesion.userId.first() ?: throw Exception("Debes iniciar sesión primero")
                val userId = idStr.toIntOrNull() ?: 0

                // Procesar con Backend real
                val sesionEstudio = repositorio.subirYProcesar(uri, titulo, contexto, userId)
                progresoSubida = 0.9f

                if (sesionEstudio != null) {
                    sesionActual = sesionEstudio
                    resumenActual = Resumen(sesionEstudio.id, sesionEstudio.resumen)
                    preguntasTest = repositorio.obtenerPreguntasDeSesion(sesionEstudio.id)
                    progresoSubida = 1f
                    delay(400)
                    alTerminar(sesionEstudio.id)
                } else {
                    error = "No se pudo procesar el documento."
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error en subirApuntes: ${e.message}", e)
                error = "Error: ${e.localizedMessage}"
            } finally {
                subiendo = false
                if (error == null) limpiarSeleccion()
            }
        }
    }

    // —— OCR: cámara → texto (Envía a API Python) ——————————————

    fun procesarImagenConOCR(bitmap: Bitmap, alTerminar: (String) -> Unit) {
        viewModelScope.launch {
            subiendo = true
            progresoSubida = 0.1f
            error = null
            nombreArchivoSeleccionado = "Apuntes escaneados"

            try {
                val textoOCR = EscaneadorOCR.extraerTexto(bitmap)
                val textoLimpio = EscaneadorOCR.limpiarTexto(textoOCR)
                progresoSubida = 0.35f

                if (textoLimpio.isBlank()) {
                    error = "No se detectó texto en la imagen."
                    return@launch
                }

                val idStr = sesion.userId.first() ?: throw Exception("Debes iniciar sesión primero")
                val userId = idStr.toIntOrNull() ?: 0

                // Mandamos el texto extraído a nuestro nuevo endpoint de la API
                val sesionEstudio = repositorio.subirYProcesarTexto(textoLimpio, "Apuntes escaneados OCR", userId)
                progresoSubida = 0.9f

                if (sesionEstudio != null) {
                    sesionActual = sesionEstudio
                    resumenActual = Resumen(sesionEstudio.id, sesionEstudio.resumen)
                    preguntasTest = repositorio.obtenerPreguntasDeSesion(sesionEstudio.id)
                    progresoSubida = 1f
                    delay(400)
                    alTerminar(sesionEstudio.id)
                }
            } catch (e: Exception) {
                error = "Error OCR: ${e.localizedMessage}"
            } finally {
                subiendo = false
            }
        }
    }

    fun cargarResultados(idSesion: String) {
        if (sesionActual?.id == idSesion && resumenActual != null) return
        viewModelScope.launch {
            cargandoResultados = true
            try {
                val sesion = sesiones.value.find { it.id == idSesion }
                if (sesion != null) {
                    sesionActual = sesion
                    resumenActual = Resumen(sesion.id, sesion.resumen)
                    preguntasTest = repositorio.obtenerPreguntasDeSesion(sesion.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando resultados: ${e.message}")
            } finally {
                cargandoResultados = false
            }
        }
    }

    fun calificarFlashcard(flashcard: Flashcard, calidad: Int) {
        viewModelScope.launch {
            val actualizada = AlgoritmoSM2.calcularProximaRevision(flashcard, calidad)
            repositorio.actualizarFlashcard(actualizada)
        }
    }

    fun observarFlashcardsDeSesion(idSesion: String) =
        repositorio.observarFlashcardsDeSesion(idSesion)

    fun exportarComoMarkdown(): Intent? {
        val sesion = sesionActual ?: return null
        return ExportadorContenido.compartirComoMarkdown(app, sesion, emptyList()) // Preguntas simplificadas
    }

    fun exportarComoPDF(): Intent? {
        val sesion = sesionActual ?: return null
        return ExportadorContenido.compartirComoPDF(app, sesion, emptyList())
    }

    fun guardarResultadoTest(idSesion: String, aciertos: Int, total: Int) {
        viewModelScope.launch {
            repositorio.guardarResultadoTest(idSesion, aciertos, total)
        }
    }
}
