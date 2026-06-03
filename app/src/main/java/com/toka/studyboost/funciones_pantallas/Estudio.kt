package com.toka.studyboost.funciones_pantallas

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
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
import com.toka.studyboost.utils.AlgoritmoSM2
import com.toka.studyboost.utils.EscaneadorOCR
import com.toka.studyboost.utils.ExportadorContenido
import com.toka.studyboost.utils.LectorDocumentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "EstudioViewModel"

/**
 * ViewModel central de la funcionalidad de estudio.
 *
 * Responsabilidades:
 * - Coordinar la extracción de texto (PDF/TXT/OCR) y el análisis con Gemini.
 * - Gestionar el estado de la UI durante la subida/procesamiento.
 * - Proporcionar flashcards con repetición espaciada (SM-2).
 * - Exponer los Flows reactivos del repositorio (historial offline-first).
 *
 * Usa [AndroidViewModel] para acceder al [Application] context (necesario
 * para Room y para los métodos de exportación).
 */
class Estudio(application: Application) : AndroidViewModel(application) {

    private val app = application as MainApplication
    private val repositorio = RepositorioEstudio(app.database)

    // â”€â”€ Estado de la UI â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    var resumenActual by mutableStateOf<Resumen?>(null)
    var preguntasTest by mutableStateOf<List<PreguntaTest>>(emptyList())
    var sesionActual by mutableStateOf<SesionEstudio?>(null)

    var progresoSubida by mutableStateOf(0f)
    var subiendo by mutableStateOf(false)
    var cargandoResultados by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    var uriArchivoSeleccionado by mutableStateOf<Uri?>(null)
    var nombreArchivoSeleccionado by mutableStateOf<String?>(null)

    // â”€â”€ Flujos reactivos (Room â†’ UI automática) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Historial de sesiones: se actualiza solo cuando Room cambia. */
    val sesiones = repositorio.observarSesiones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Badge: número de flashcards pendientes de revisar hoy. */
    val flashcardsParaHoyCount = repositorio.observarContadorFlashcardsHoy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // â”€â”€ Selección de archivo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Subida y procesamiento (PDF / TXT) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Flujo completo de procesamiento:
     * 1. Extraer texto del documento (PDFBox o lector de texto plano).
     * 2. Validar que el texto no esté vacío.
     * 3. Enviar a Gemini (truncando si es necesario).
     * 4. Guardar sesión + preguntas + flashcards en Room.
     * 5. Navegar a resultados.
     */
    fun subirApuntes(contexto: android.content.Context, alTerminar: (String) -> Unit) {
        val uri = uriArchivoSeleccionado ?: return

        viewModelScope.launch {
            subiendo = true
            progresoSubida = 0.1f
            error = null

            try {
                // 1. Extraer texto
                val textoCompleto = LectorDocumentos.extraerTexto(contexto, uri)
                progresoSubida = 0.3f

                // 2. Validaciones
                if (textoCompleto.isBlank()) {
                    error = "No se pudo extraer texto del documento. " +
                            "El PDF puede estar escaneado como imagen (sin texto seleccionable). " +
                            "Prueba la función de cámara para escanear."
                    return@launch
                }

                val titulo = nombreArchivoSeleccionado ?: "Documento sin título"

                // 3. Procesar con IA y guardar en Room
                val sesion = repositorio.procesarYGuardar(textoCompleto, titulo)
                progresoSubida = 0.9f

                if (sesion != null) {
                    // Cargar en memoria para la pantalla de resultados
                    sesionActual = sesion
                    resumenActual = Resumen(sesion.id, sesion.resumen)
                    preguntasTest = repositorio.obtenerPreguntasDeSesion(sesion.id)
                    progresoSubida = 1f
                    delay(400)
                    alTerminar(sesion.id)
                } else {
                    error = "La IA no pudo procesar el documento. " +
                            "Verifica tu conexión o intenta con un archivo más pequeño."
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error en subirApuntes: ${e.message}", e)
                error = "Error inesperado: ${e.localizedMessage}"
            } finally {
                subiendo = false
                if (error == null) limpiarSeleccion()
            }
        }
    }

    // â”€â”€ OCR: cámara â†’ texto â†’ IA â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Procesa una foto de apuntes físicos:
     * 1. ML Kit extrae el texto de la imagen (on-device, sin internet).
     * 2. Se limpia el texto (quitar guiones de final de línea, etc.).
     * 3. Se envía a Gemini igual que un PDF.
     */
    fun procesarImagenConOCR(bitmap: Bitmap, alTerminar: (String) -> Unit) {
        viewModelScope.launch {
            subiendo = true
            progresoSubida = 0.1f
            error = null
            nombreArchivoSeleccionado = "Apuntes escaneados"

            try {
                // 1. OCR on-device
                val textoOCR = EscaneadorOCR.extraerTexto(bitmap)
                val textoLimpio = EscaneadorOCR.limpiarTexto(textoOCR)
                progresoSubida = 0.35f

                if (textoLimpio.isBlank()) {
                    error = "No se detectó texto en la imagen. " +
                            "Asegúrate de tener buena iluminación y enfoque."
                    return@launch
                }

                Log.d(TAG, "OCR extraído: ${textoLimpio.length} chars")

                // 2. Procesar con IA (mismo flujo que PDF)
                val sesion = repositorio.procesarYGuardar(textoLimpio, "Apuntes escaneados")
                progresoSubida = 0.9f

                if (sesion != null) {
                    sesionActual = sesion
                    resumenActual = Resumen(sesion.id, sesion.resumen)
                    preguntasTest = repositorio.obtenerPreguntasDeSesion(sesion.id)
                    progresoSubida = 1f
                    delay(400)
                    alTerminar(sesion.id)
                } else {
                    error = "La IA no pudo procesar las notas escaneadas."
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error en OCR: ${e.message}", e)
                error = "Error al procesar la imagen: ${e.localizedMessage}"
            } finally {
                subiendo = false
                if (error == null) limpiarSeleccion()
            }
        }
    }

    // â”€â”€ Resultados (carga desde historial) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Carga los resultados de una sesión del historial (Room).
     * Si la sesión es "ai_generated", ya están en memoria â€” no hace nada.
     */
    fun cargarResultados(idSesion: String) {
        if (sesionActual?.id == idSesion && resumenActual != null) return

        viewModelScope.launch {
            cargandoResultados = true
            try {
                // Buscar en el StateFlow en memoria (ya cargado desde Room)
                val sesion = sesiones.value.find { it.id == idSesion }
                if (sesion != null) {
                    sesionActual = sesion
                    resumenActual = Resumen(sesion.id, sesion.resumen)
                    preguntasTest = repositorio.obtenerPreguntasDeSesion(sesion.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando resultados: ${e.message}", e)
            } finally {
                cargandoResultados = false
            }
        }
    }

    // â”€â”€ Flashcards con SM-2 â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Registra la calificación del usuario para una flashcard y recalcula
     * cuándo debe mostrarse de nuevo usando el algoritmo SM-2.
     *
     * @param flashcard La tarjeta que acaba de responder.
     * @param calidad   Puntuación 0-5 (0=no recordé nada, 5=perfecto).
     */
    fun calificarFlashcard(flashcard: Flashcard, calidad: Int) {
        viewModelScope.launch {
            val actualizada = AlgoritmoSM2.calcularProximaRevision(flashcard, calidad)
            repositorio.actualizarFlashcard(actualizada)
            Log.d(TAG, "Flashcard ${flashcard.id} actualizada: intervalo=${actualizada.intervalo}d")
        }
    }

    fun observarFlashcardsDeSesion(idSesion: String) =
        repositorio.observarFlashcardsDeSesion(idSesion)

    // â”€â”€ Exportación â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Genera un Intent de compartir con el resumen y preguntas en formato Markdown.
     * Devuelve `null` si no hay sesión activa.
     */
    fun exportarComoMarkdown(): Intent? {
        val sesion = sesionActual ?: return null
        return try {
            ExportadorContenido.compartirComoMarkdown(
                context = app,
                sesion = sesion,
                preguntas = preguntasTest.map { p ->
                    com.toka.studyboost.datos.PreguntaGuardada(
                        idSesion = sesion.id,
                        enunciado = p.enunciado,
                        opcionesJson = com.google.gson.Gson().toJson(p.opciones),
                        respuestaCorrecta = p.respuestaCorrecta
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exportando Markdown: ${e.message}", e)
            null
        }
    }

    /**
     * Genera un Intent de compartir con el resumen en formato PDF.
     */
    fun exportarComoPDF(): Intent? {
        val sesion = sesionActual ?: return null
        return try {
            ExportadorContenido.compartirComoPDF(
                context = app,
                sesion = sesion,
                preguntas = preguntasTest.map { p ->
                    com.toka.studyboost.datos.PreguntaGuardada(
                        idSesion = sesion.id,
                        enunciado = p.enunciado,
                        opcionesJson = com.google.gson.Gson().toJson(p.opciones),
                        respuestaCorrecta = p.respuestaCorrecta
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exportando PDF: ${e.message}", e)
            null
        }
    }
}
