package com.toka.studyboost.funciones_pantallas

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.toka.studyboost.MainApplication
import com.toka.studyboost.datos.Usuario
import com.toka.studyboost.red.RepositorioEstudio
import com.toka.studyboost.red.SesionUsuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel de autenticación.
 * Gestiona el estado de sesión del usuario usando DataStore para persistencia.
 *
 * Nota: la autenticación es actualmente simulada (no hay backend real).
 * Para integrar un backend real, reemplazar los métodos de [RepositorioEstudio]
 * que usan `delay()` por llamadas HTTP reales.
 */
class Autenticacion(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioEstudio((application as MainApplication).database)
    private val sesion = SesionUsuario(application)

    var usuarioActual by mutableStateOf<Usuario?>(null)
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var sesionCargada by mutableStateOf(false)

    init {
        verificarSesion()
    }

    private fun verificarSesion() {
        viewModelScope.launch {
            val id = sesion.userId.first()
            val nombre = sesion.userName.first()
            val email = sesion.userEmail.first()
            if (id != null && nombre != null && email != null) {
                usuarioActual = Usuario(id, nombre, email)
            }
            sesionCargada = true
        }
    }

    fun iniciarSesion(email: String, contrasena: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                val usuario = repositorio.iniciarSesion(email, contrasena)
                sesion.guardarSesion(usuario.id, usuario.nombre, usuario.email)
                usuarioActual = usuario
                alTerminar()
            } catch (e: Exception) {
                error = "Error al iniciar sesión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    fun registrarse(nombre: String, email: String, contrasena: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                val usuario = repositorio.registrarUsuario(nombre, email, contrasena)
                sesion.guardarSesion(usuario.id, usuario.nombre, usuario.email)
                usuarioActual = usuario
                alTerminar()
            } catch (e: Exception) {
                error = "Error al registrarse: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    fun cambiarContrasena(actual: String, nueva: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                repositorio.cambiarContrasena(actual, nueva)
                alTerminar()
            } catch (e: Exception) {
                error = "Error al cambiar contraseña: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            sesion.cerrarSesion()
            usuarioActual = null
            alTerminar()
        }
    }
}
