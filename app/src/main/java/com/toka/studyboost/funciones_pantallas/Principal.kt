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

    /** Estado de búsqueda */
    var textoBusqueda by mutableStateOf("")

    var cargando by mutableStateOf(false)

    /**
     * Flujo de sesiones originales desde Room.
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
     */
    val sesionesComoApuntes: StateFlow<List<Apunte>> = sesionesRaw
        .map { sesiones -> sesiones.map { it.toApunte() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Lista filtrada usando derivedStateOf para evitar recomposiciones innecesarias. */
    val apuntesFiltrados by derivedStateOf {
        val lista = sesionesComoApuntes.value
        if (textoBusqueda.isBlank()) lista
        else lista.filter { it.titulo.contains(textoBusqueda, ignoreCase = true) }
    }

    // Estadísticas dinámicas basadas en Room
    val totalApuntes by derivedStateOf { sesionesRaw.value.size.toString() }
    val totalTests by derivedStateOf { sesionesRaw.value.sumOf { it.totalPreguntas }.toString() }

    fun eliminarSesion(apunte: Apunte) {
        viewModelScope.launch {
            try {
                repositorio.eliminarSesion(SesionEstudio(
                    id = apunte.id,
                    titulo = apunte.titulo,
                    resumen = apunte.contenido,
                    totalPreguntas = 0,
                    fechaCreacion = 0L
                ))
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
