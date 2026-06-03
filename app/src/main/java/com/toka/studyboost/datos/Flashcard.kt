package com.toka.studyboost.datos

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tarjeta de estudio (flashcard) generada automáticamente por la IA.
 * Implementa los campos necesarios para el algoritmo SM-2 de repetición espaciada:
 *
 * - [intervalo]: días hasta la próxima revisión.
 * - [repeticiones]: número de veces respondida correctamente seguidas.
 * - [factorFacilidad]: EF (Easiness Factor) — empieza en 2.5, mínimo 1.3.
 * - [proximaRevision]: timestamp Unix de cuándo mostrarla de nuevo.
 *
 * El ViewModel [FlashcardViewModel] usa [AlgoritmoSM2] para actualizar estos campos
 * cada vez que el usuario califica su respuesta (0-5).
 */
@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(
        entity = SesionEstudio::class,
        parentColumns = ["id"],
        childColumns = ["idSesion"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("idSesion"), Index("proximaRevision")]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idSesion: String,
    val pregunta: String,
    val respuesta: String,
    // —— Campos SM-2 ————————————————————————————————————————————————————————————
    val intervalo: Int = 1,
    val repeticiones: Int = 0,
    val factorFacilidad: Float = 2.5f,
    val proximaRevision: Long = System.currentTimeMillis()
)
