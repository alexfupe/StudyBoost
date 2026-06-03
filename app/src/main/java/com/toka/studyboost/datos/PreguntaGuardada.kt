package com.toka.studyboost.datos

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pregunta de test guardada localmente, asociada a una [SesionEstudio].
 * Las opciones se serializan como JSON string para evitar tablas adicionales.
 * ON DELETE CASCADE: si se elimina la sesión, se eliminan sus preguntas también.
 */
@Entity(
    tableName = "preguntas_guardadas",
    foreignKeys = [ForeignKey(
        entity = SesionEstudio::class,
        parentColumns = ["id"],
        childColumns = ["idSesion"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("idSesion")]
)
data class PreguntaGuardada(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idSesion: String,
    val enunciado: String,
    /** JSON array serializado: ["opción A", "opción B", "opción C"] */
    val opcionesJson: String,
    val respuestaCorrecta: Int
)
