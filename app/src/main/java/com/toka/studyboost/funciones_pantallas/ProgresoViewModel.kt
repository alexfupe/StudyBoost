package com.toka.studyboost.funciones_pantallas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toka.studyboost.MainApplication
import com.toka.studyboost.red.RepositorioEstudio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Datos estadísticos agregados del usuario.
 *
 * @property totalDocumentos    Número de documentos procesados (sesiones guardadas).
 * @property promedioAciertos   Porcentaje medio de aciertos en los tests (0â€“100).
 * @property tiempoEstudioHoras Tiempo estimado de estudio acumulado en horas.
 */
data class EstadisticasUsuario(
    val totalDocumentos: Int,
    val promedioAciertos: Float,
    val tiempoEstudioHoras: Float
)

/**
 * ViewModel dedicado a la pantalla de progreso.
 *
 * Expone [estadisticas] como un [StateFlow] reactivo y ofrece
 * [cargarEstadisticas] para calcular y actualizar los datos desde Room.
 */
class ProgresoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioEstudio(
        (application as MainApplication).database
    )

    private val _estadisticas = MutableStateFlow<EstadisticasUsuario?>(null)

    /** Estadísticas del usuario; `null` mientras se están cargando. */
    val estadisticas: StateFlow<EstadisticasUsuario?> = _estadisticas.asStateFlow()

    /**
     * Calcula las estadísticas agregadas leyendo todas las sesiones de Room.
     *
     * - totalDocumentos    â†’ número de sesiones guardadas.
     * - promedioAciertos   â†’ el modelo actual no almacena aciertos por sesión;
     *                        se expone como 0f hasta que se añada esa columna.
     * - tiempoEstudioHoras â†’ estimado de 0.25 h (15 min) por documento procesado.
     */
    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                // Tomamos el primer snapshot del Flow (Room emite inmediatamente)
                val sesiones = repositorio.observarSesiones().first()

                val total = sesiones.size
                // Estimación: 15 minutos (0.25 h) por documento procesado
                val horas = total * 0.25f
                // Porcentaje de aciertos no está aún en el modelo; se deja en 0
                val promedio = 0f

                _estadisticas.value = EstadisticasUsuario(
                    totalDocumentos = total,
                    promedioAciertos = promedio,
                    tiempoEstudioHoras = horas
                )
            } catch (e: Exception) {
                // Si falla la carga mostramos ceros en lugar de bloquear la UI
                _estadisticas.value = EstadisticasUsuario(
                    totalDocumentos = 0,
                    promedioAciertos = 0f,
                    tiempoEstudioHoras = 0f
                )
            }
        }
    }
}
