package com.toka.studyboost.utils

import com.toka.studyboost.datos.Flashcard
import java.util.concurrent.TimeUnit

/**
 * Implementación del algoritmo SM-2 (SuperMemo 2) para repetición espaciada.
 *
 * Cómo funciona:
 * - El usuario califica su respuesta con una "calidad" de 0 a 5:
 *     0 = Blackout total (no recordé nada)
 *     1 = Incorrecto pero al verlo lo reconocí
 *     2 = Incorrecto, pero la respuesta correcta fue fácil de entender
 *     3 = Correcto con mucho esfuerzo
 *     4 = Correcto con poco esfuerzo
 *     5 = Perfecto, sin dudarlo
 *
 * - Si calidad < 3: se reinicia el intervalo (volver a aprender desde 1 día).
 * - Si calidad >= 3: el intervalo crece según el Factor de Facilidad (EF).
 *
 * El EF determina qué tan rápido escala el intervalo:
 *   EF mínimo: 1.3 (tarjeta difícil, intervalos crecen lentamente)
 *   EF inicial: 2.5 (neutral)
 *   EF máximo: no hay techo, pero rara vez supera 3.0
 */
object AlgoritmoSM2 {

    /**
     * Calcula el nuevo estado de una flashcard tras calificarla.
     *
     * @param flashcard La tarjeta actual con su estado SM-2.
     * @param calidad   Puntuación del usuario: 0 (mal) a 5 (perfecto).
     * @return Una nueva copia de [Flashcard] con los campos SM-2 actualizados.
     */
    fun calcularProximaRevision(flashcard: Flashcard, calidad: Int): Flashcard {
        require(calidad in 0..5) { "La calidad debe estar entre 0 y 5, recibido: $calidad" }

        return if (calidad < 3) {
            // Respuesta incorrecta: reiniciar el ciclo completo
            flashcard.copy(
                repeticiones = 0,
                intervalo = 1,
                // EF no se toca cuando la respuesta es incorrecta
                proximaRevision = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L)
            )
        } else {
            // Respuesta correcta: calcular nuevo intervalo y EF
            val nuevoEF = calcularNuevoEF(flashcard.factorFacilidad, calidad)
            val nuevoIntervalo = calcularNuevoIntervalo(flashcard.repeticiones, flashcard.intervalo, nuevoEF)

            flashcard.copy(
                repeticiones = flashcard.repeticiones + 1,
                intervalo = nuevoIntervalo,
                factorFacilidad = nuevoEF,
                proximaRevision = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(nuevoIntervalo.toLong())
            )
        }
    }

    /**
     * Fórmula SM-2 para actualizar el Factor de Facilidad:
     * EF' = EF + (0.1 - (5 - calidad) * (0.08 + (5 - calidad) * 0.02))
     * Coerced a un mínimo de 1.3.
     */
    private fun calcularNuevoEF(ef: Float, calidad: Int): Float {
        val delta = 0.1 - (5 - calidad) * (0.08 + (5 - calidad) * 0.02)
        return (ef + delta).toFloat().coerceAtLeast(1.3f)
    }

    /**
     * Intervalos de repetición:
     * - Primera vez correcta (repeticiones == 0) → 1 día
     * - Segunda vez correcta (repeticiones == 1) → 6 días
     * - Sucesivas → intervalo anterior × EF (crecimiento exponencial)
     */
    private fun calcularNuevoIntervalo(repeticiones: Int, intervaloActual: Int, ef: Float): Int {
        return when (repeticiones) {
            0 -> 1
            1 -> 6
            else -> (intervaloActual * ef).toInt().coerceAtLeast(1)
        }
    }

    /**
     * Filtra la lista para devolver solo las tarjetas cuya revisión ya llegó.
     * Útil para mostrar el badge "X tarjetas pendientes hoy".
     */
    fun tarjetasParaHoy(flashcards: List<Flashcard>): List<Flashcard> {
        val ahora = System.currentTimeMillis()
        return flashcards.filter { it.proximaRevision <= ahora }
    }
}
