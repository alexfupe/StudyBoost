package com.toka.studyboost.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos Room de StudyBoost.
 *
 * Contiene 2 tablas:
 * - [SesionEstudio]: historial de documentos procesados con la IA.
 * - [PreguntaGuardada]: preguntas de test asociadas a cada sesión.
 *
 * El singleton se crea una sola vez desde [MainApplication] y se reutiliza
 * en toda la app. Nunca crear instancias adicionales — Room no es thread-safe
 * si hay múltiples instancias.
 */
@Database(
    entities = [SesionEstudio::class, PreguntaGuardada::class],
    version = 3,
    exportSchema = false
)
abstract class StudyBoostDatabase : RoomDatabase() {

    abstract fun sesionEstudioDao(): SesionEstudioDao
    abstract fun preguntaGuardadaDao(): PreguntaGuardadaDao

    companion object {
        @Volatile
        private var INSTANCE: StudyBoostDatabase? = null

        fun getInstance(context: Context): StudyBoostDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyBoostDatabase::class.java,
                    "studyboost.db"
                )
                    // En producción, reemplazar por migraciones explícitas
                    // para no perder datos del usuario al cambiar el esquema.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
