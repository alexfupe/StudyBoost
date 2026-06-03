package com.toka.studyboost.datos

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para [Flashcard].
 * Las dos queries más importantes del sistema de repetición espaciada:
 * 1. [observarParaRevisar]: tarjetas cuya fecha de revisión ya llegó (modo estudio).
 * 2. [observarPorSesion]: todas las tarjetas de un documento (modo gestión).
 */
@Dao
interface FlashcardDao {

    /**
     * Devuelve las flashcards que deben revisarse hoy o antes.
     * El índice en [Flashcard.proximaRevision] hace esta query muy eficiente.
     */
    @Query("SELECT * FROM flashcards WHERE proximaRevision <= :ahora ORDER BY proximaRevision ASC")
    fun observarParaRevisar(ahora: Long = System.currentTimeMillis()): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE idSesion = :idSesion")
    fun observarPorSesion(idSesion: String): Flow<List<Flashcard>>

    /** Upsert: inserta si es nueva, actualiza si ya existe (por id). */
    @Upsert
    suspend fun guardarOActualizar(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(flashcards: List<Flashcard>)

    @Query("SELECT COUNT(*) FROM flashcards WHERE proximaRevision <= :ahora")
    fun contarParaHoy(ahora: Long = System.currentTimeMillis()): Flow<Int>
}
