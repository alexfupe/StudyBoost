package com.toka.studyboost.datos

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** DAO para preguntas guardadas de cada sesión. */
@Dao
interface PreguntaGuardadaDao {

    @Query("SELECT * FROM preguntas_guardadas WHERE idSesion = :idSesion")
    fun observarPorSesion(idSesion: String): Flow<List<PreguntaGuardada>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(preguntas: List<PreguntaGuardada>)

    @Query("DELETE FROM preguntas_guardadas WHERE idSesion = :idSesion")
    suspend fun eliminarPorSesion(idSesion: String)
}
