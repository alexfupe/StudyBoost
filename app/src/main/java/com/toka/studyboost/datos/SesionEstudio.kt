package com.toka.studyboost.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una sesión de estudio completa guardada localmente.
 * Cada vez que el usuario procesa un documento con la IA, se crea un registro aquí.
 * Esto permite el historial local y el modo offline (los resúmenes ya procesados
 * están siempre disponibles, sin necesidad de internet).
 */
@Entity(tableName = "sesiones_estudio")
data class SesionEstudio(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val resumen: String,
    val totalPreguntas: Int,
    val mimeTypeArchivo: String = "application/pdf"
)
