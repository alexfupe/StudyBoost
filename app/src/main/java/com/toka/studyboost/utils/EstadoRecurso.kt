package com.toka.studyboost.utils

/**
 * Sealed class para representar el estado de una operación asíncrona
 * que puede venir de la red o de la caché local (Room).
 *
 * Uso típico en el ViewModel:
 * ```kotlin
 * _uiState.value = EstadoRecurso.Cargando
 * // ... operación
 * _uiState.value = EstadoRecurso.Exito(datos, desdeCache = true)
 * ```
 */
sealed class EstadoRecurso<out T> {
    /** La operación está en progreso. */
    data object Cargando : EstadoRecurso<Nothing>()

    /**
     * La operación completó con éxito.
     * @param datos El resultado de la operación.
     * @param desdeCache true si los datos provienen de Room (sin internet).
     */
    data class Exito<T>(
        val datos: T,
        val desdeCache: Boolean = false
    ) : EstadoRecurso<T>()

    /**
     * La operación falló.
     * @param mensaje Descripción del error para mostrar al usuario.
     */
    data class Error(val mensaje: String) : EstadoRecurso<Nothing>()
}
