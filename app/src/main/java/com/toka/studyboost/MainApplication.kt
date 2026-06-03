package com.toka.studyboost

import android.app.Application
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.toka.studyboost.datos.StudyBoostDatabase

/**
 * Application class â€” punto de inicio de la app.
 * Responsabilidades:
 *  1. Inicializar PDFBoxResourceLoader UNA SOLA VEZ (evita overhead en cada extracción).
 *  2. Exponer la instancia de la base de datos Room al resto de la app.
 */
class MainApplication : Application() {

    /** Instancia singleton de la base de datos Room. */
    val database: StudyBoostDatabase by lazy {
        StudyBoostDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Inicializar PDFBox una sola vez al arrancar la app
        try {
            PDFBoxResourceLoader.init(this)
            Log.d("StudyBoost", "PDFBox inicializado correctamente")
        } catch (e: Exception) {
            Log.e("StudyBoost", "Error inicializando PDFBox: ${e.message}", e)
        }
    }
}
