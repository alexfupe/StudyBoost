package com.toka.studyboost.funciones_pantallas

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toka.studyboost.MainApplication
import com.toka.studyboost.datos.Apunte
import com.toka.studyboost.datos.SesionEstudio
import com.toka.studyboost.red.RepositorioEstudio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para la pantalla principal.
 *
 * Fuente de datos: Room (a través del repositorio) â†’ los datos del historial
 * se actualizan automáticamente cada vez que el usuario sube un nuevo documento.
 * No es necesario llamar a "cargarApuntes()" manualmente â€” el Flow de Room
 * emite el estado actual al suscribirse.
 */
class Principal(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioEstudio((application as MainApplication).database)

    /** Estado de búsqueda */
    var textoBusqueda by mutableStateOf("")

    /**
     * Flujo de sesiones desde Room, mapeado a [Apunte] para compatibilidad con la UI.
     * [SharingStarted.WhileSubscribed(5_000)]: mantiene el Flow activo 5s después de
     * que la UI deja de observarlo (para rotaciones de pantalla sin re-query).
     */
    val sesionesComoApuntes = repositorio.observarSesiones()
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
    val totalApuntes by derivedStateOf { sesionesComoApuntes.value.size.toString() }
    val totalTests by derivedStateOf { (sesionesComoApuntes.value.sumOf { it.contenido.toIntOrNull() ?: 0 }).toString() }
    val rachaDias by derivedStateOf { "3 días" } // TODO: calcular racha real

    var cargando by mutableStateOf(false)

    // Compatibilidad â€” ya no hace falta llamar esto, Room emite automáticamente
    fun cargarApuntes() { /* Room ya emite automáticamente al colectar sesionesComoApuntes */ }

    fun eliminarSesion(apunte: Apunte) {
        viewModelScope.launch {
            val sesion = SesionEstudio(
                id = apunte.id,
                titulo = apunte.titulo,
                resumen = apunte.contenido,
                totalPreguntas = 0,
                fechaCreacion = 0L
            )
            repositorio.eliminarSesion(sesion)
        }
    }

    // â”€â”€ Mapper â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun SesionEstudio.toApunte(): Apunte {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(fechaCreacion))
        return Apunte(
            id = id,
            titulo = titulo,
            fecha = fecha,
            contenido = totalPreguntas.toString() // Reutilizamos contenido para el conteo
        )
    }
}
