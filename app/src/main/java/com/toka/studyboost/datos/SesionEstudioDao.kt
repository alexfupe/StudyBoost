package com.toka.studyboost.datos

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para [SesionEstudio].
 * Todas las consultas que devuelven [Flow] son reactivas — Room notifica automáticamente
 * a los colectores cuando los datos cambian, sin polling manual.
 */
@Dao
interface SesionEstudioDao {

    /** Devuelve todas las sesiones ordenadas de más reciente a más antigua. */
    @Query("SELECT * FROM sesiones_estudio ORDER BY fechaCreacion DESC")
    fun observarTodas(): Flow<List<SesionEstudio>>

    /** Inserta o reemplaza si ya existe (idempotente — seguro de llamar varias veces). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sesion: SesionEstudio)

    @Delete
    suspend fun eliminar(sesion: SesionEstudio)

    @Query("SELECT * FROM sesiones_estudio WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): SesionEstudio?

    @Query("SELECT * FROM sesiones_estudio WHERE titulo = :titulo LIMIT 1")
    suspend fun obtenerPorTitulo(titulo: String): SesionEstudio?

    @Query("SELECT COUNT(*) FROM sesiones_estudio")
    fun contarSesiones(): Flow<Int>
}
