package com.toka.studyboost.funciones_pantallas

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toka.studyboost.MainApplication
import com.toka.studyboost.datos.Apunte
import com.toka.studyboost.datos.SesionEstudio
import com.toka.studyboost.red.RepositorioEstudio
import com.toka.studyboost.red.MockRepositorioEstudio
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "PrincipalViewModel"

/**
 * ViewModel para la pantalla principal.
 */
class Principal(application: Application) : AndroidViewModel(application) {

    private val app = application as MainApplication
    private val repositorio: RepositorioEstudio = MockRepositorioEstudio(app.database)

    /** Estado de búsqueda — observable desde Compose */
    var textoBusqueda by mutableStateOf("")

    var cargando by mutableStateOf(false)
    var refrescando by mutableStateOf(false)

    /**
     * Flujo de sesiones originales desde Room.
     * Room emite automáticamente cada vez que la tabla cambia (INSERT/DELETE).
     */
    val sesionesRaw: StateFlow<List<SesionEstudio>> = repositorio.observarSesiones()
        .onEach { Log.d(TAG, "Nuevas sesiones emitidas: ${it.size}") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Mapeado a [Apunte] para la UI.
     * Este es el flow COMPLETO sin filtrar — el filtrado se hace en la UI
     * para que Compose pueda reaccionar correctamente a los cambios de [textoBusqueda].
     */
    val sesionesComoApuntes: StateFlow<List<Apunte>> = sesionesRaw
        .map { sesiones -> sesiones.map { it.toApunte() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Total de apuntes/resúmenes guardados — reactivo al flujo de Room.
     * Se calcula como StateFlow para que la UI pueda colectarlo como State.
     */
    val totalApuntesFlow: StateFlow<Int> = sesionesRaw
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Total de preguntas de test disponibles — suma de todas las sesiones.
     */
    val totalTestsFlow: StateFlow<Int> = sesionesRaw
        .map { sesiones -> sesiones.sumOf { it.totalPreguntas } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun refrescar() {
        viewModelScope.launch {
            refrescando = true
            // Pequeño delay para que el indicador sea visible al usuario
            delay(600)
            refrescando = false
        }
    }

    fun eliminarSesion(apunteId: String) {
        viewModelScope.launch {
            try {
                val sesion = sesionesRaw.value.find { it.id == apunteId }
                if (sesion != null) {
                    repositorio.eliminarSesion(sesion)
                    Log.d(TAG, "Sesión eliminada: $apunteId")
                } else {
                    Log.w(TAG, "No se encontró sesión con id: $apunteId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar sesión: ${e.message}")
            }
        }
    }

    // —— Mapper ——————————————————————————————————————————————————————————————————————

    private fun SesionEstudio.toApunte(): Apunte {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(fechaCreacion))
        
        // Si hay un resultado de test, lo añadimos al contenido/subtítulo
        val infoTest = if (ultimoAcierto != null && ultimoTotal != null) {
            " | Test: $ultimoAcierto/$ultimoTotal"
        } else ""

        return Apunte(
            id = id,
            titulo = titulo,
            fecha = fecha + infoTest,
            contenido = resumen
        )
    }
}
