package com.toka.studyboost.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.FileProvider
import com.toka.studyboost.datos.PreguntaGuardada
import com.toka.studyboost.datos.SesionEstudio
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ExportadorContenido"

/**
 * Exporta los resultados de la IA a formatos compartibles.
 *
 * Formatos disponibles:
 * 1. **Markdown** (.md) — Ligero, legible en cualquier editor, compatible con Notion/Obsidian.
 * 2. **PDF** (.pdf) — Usa [android.graphics.pdf.PdfDocument] nativo; sin dependencias externas.
 *
 * Ambos métodos devuelven un [Intent] listo para [Context.startActivity],
 * que abre el selector de apps del sistema (compartir con WhatsApp, Drive, etc.).
 */
object ExportadorContenido {

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // —— Markdown ——————————————————————————————————————————————

    /**
     * Genera un archivo Markdown con el resumen y las preguntas, y devuelve un Intent
     * de compartir para enviarlo a Notion, WhatsApp, Drive, etc.
     */
    fun compartirComoMarkdown(
        context: Context,
        sesion: SesionEstudio,
        preguntas: List<PreguntaGuardada>
    ): Intent {
        val fecha = dateFormat.format(Date(sesion.fechaCreacion))
        val contenido = construirMarkdown(sesion, preguntas, fecha)

        val nombreArchivo = "${sesion.titulo.sanitizarParaArchivo()}_$fecha.md"
        val archivo = guardarEnCache(context, nombreArchivo, contenido)

        return crearIntentCompartir(context, archivo, "text/markdown")
    }

    private fun construirMarkdown(
        sesion: SesionEstudio,
        preguntas: List<PreguntaGuardada>,
        fecha: String
    ): String = buildString {
        appendLine("# ${sesion.titulo}")
        appendLine("*Generado por StudyBoost — $fecha*")
        appendLine()
        appendLine("---")
        appendLine()
        appendLine("## 📝 Resumen")
        appendLine()
        appendLine(sesion.resumen)
        appendLine()

        if (preguntas.isNotEmpty()) {
            appendLine("---")
            appendLine()
            appendLine("## ❓ Preguntas de Repaso")
            appendLine()
            preguntas.forEachIndexed { i, pregunta ->
                val opciones = deserializarOpciones(pregunta.opcionesJson)
                appendLine("**${i + 1}. ${pregunta.enunciado}**")
                opciones.forEachIndexed { j, opcion ->
                    val marca = if (j == pregunta.respuestaCorrecta) "✅" else "○"
                    appendLine("   $marca $opcion")
                }
                appendLine()
            }
        }

        appendLine("---")
        appendLine("*Exportado desde StudyBoost · studyboost.app*")
    }

    // —— PDF ——————————————————————————————————————————————————————

    /**
     * Genera un PDF básico con el resumen y las preguntas usando [PdfDocument] nativo.
     * Sin librerías externas — funciona en toda versión de Android ≥ API 19.
     */
    fun compartirComoPDF(
        context: Context,
        sesion: SesionEstudio,
        preguntas: List<PreguntaGuardada>
    ): Intent {
        val fecha = dateFormat.format(Date(sesion.fechaCreacion))
        val pdfDocument = PdfDocument()

        try {
            val paintTitulo = Paint().apply { textSize = 22f; isFakeBoldText = true }
            val paintSubtitulo = Paint().apply { textSize = 16f; isFakeBoldText = true }
            val paintCuerpo = Paint().apply { textSize = 13f }
            val paintMeta = Paint().apply { textSize = 11f; color = android.graphics.Color.GRAY }

            // Página 1: Resumen
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 en puntos
            val pagina = pdfDocument.startPage(pageInfo)
            val canvas = pagina.canvas

            var y = 60f
            val margenIzq = 50f
            val anchoUtil = 495f // 595 - 2*50

            // Título
            canvas.drawText(sesion.titulo.take(60), margenIzq, y, paintTitulo)
            y += 28f
            canvas.drawText("Generado por StudyBoost · $fecha", margenIzq, y, paintMeta)
            y += 30f

            // Línea separadora
            canvas.drawLine(margenIzq, y, margenIzq + anchoUtil, y, paintMeta)
            y += 20f

            // Resumen
            canvas.drawText("RESUMEN", margenIzq, y, paintSubtitulo)
            y += 22f

            // Texto con wrapping manual (chunks de ~85 caracteres)
            sesion.resumen.chunked(85).take(30).forEach { linea ->
                if (y > 800f) return@forEach // Evitar desbordamiento de página
                canvas.drawText(linea, margenIzq, y, paintCuerpo)
                y += 18f
            }

            pdfDocument.finishPage(pagina)

            // Guardar y compartir
            val nombreArchivo = "${sesion.titulo.sanitizarParaArchivo()}_$fecha.pdf"
            val archivo = File(context.cacheDir, nombreArchivo)
            pdfDocument.writeTo(archivo.outputStream())

            return crearIntentCompartir(context, archivo, "application/pdf")

        } catch (e: Exception) {
            Log.e(TAG, "Error generando PDF: ${e.message}", e)
            throw e
        } finally {
            pdfDocument.close()
        }
    }

    // —— Helpers ——————————————————————————————————————————————————————

    private fun guardarEnCache(context: Context, nombreArchivo: String, contenido: String): File {
        return File(context.cacheDir, nombreArchivo).also { it.writeText(contenido) }
    }

    private fun crearIntentCompartir(context: Context, archivo: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun deserializarOpciones(json: String): List<String> {
        return try {
            val tipo = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, tipo)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Elimina caracteres inválidos para nombres de archivo. */
    private fun String.sanitizarParaArchivo(): String =
        replace(Regex("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s_-]"), "")
            .replace(" ", "_")
            .take(50)
}
