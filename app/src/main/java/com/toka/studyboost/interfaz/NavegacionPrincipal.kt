package com.toka.studyboost.interfaz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.toka.studyboost.ui.theme.AzulBrillante
import com.toka.studyboost.funciones_pantallas.Autenticacion
import com.toka.studyboost.funciones_pantallas.Estudio
import com.toka.studyboost.funciones_pantallas.Principal
import com.toka.studyboost.funciones_pantallas.ProgresoViewModel

@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()
    
    val logicaAuth: Autenticacion = viewModel()
    val logicaPrincipal: Principal = viewModel()
    val logicaEstudio: Estudio = viewModel()
    val logicaProgreso: ProgresoViewModel = viewModel()

    if (!logicaAuth.sesionCargada) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulBrillante)
        }
    } else {
        val destinoInicial = if (logicaAuth.usuarioActual != null) "principal" else "bienvenida"
        
        NavHost(navController = navController, startDestination = destinoInicial) {
            composable("bienvenida") {
                PantallaBienvenida(alContinuar = { navController.navigate("login") })
            }
            
            composable("login") {
                PantallaInicioSesion(
                    logica = logicaAuth,
                    alIniciarSesion = { navController.navigate("principal") {
                        popUpTo("bienvenida") { inclusive = true }
                    }},
                    alIrARegistro = { navController.navigate("registro") }
                )
            }
            
            composable("registro") {
                PantallaRegistro(
                    logica = logicaAuth,
                    alRegistrarse = { navController.navigate("principal") {
                        popUpTo("bienvenida") { inclusive = true }
                    }},
                    alVolverALogin = { navController.popBackStack() }
                )
            }
            
            composable("principal") {
                PantallaPrincipal(
                    logica = logicaPrincipal,
                    nombreUsuario = logicaAuth.usuarioActual?.nombre ?: "Estudiante",
                    alSubirApuntes = { navController.navigate("subir") },
                    alVerDocumento = { id -> navController.navigate("resultados/$id") },
                    alIrAPerfil = { navController.navigate("perfil") },
                    alIrAProgreso = { navController.navigate("progreso") },
                    alCerrarSesion = { 
                        logicaAuth.cerrarSesion {
                            navController.navigate("login") {
                                popUpTo("principal") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("perfil") {
                PantallaPerfil(
                    logica = logicaAuth,
                    alVolver = { navController.popBackStack() }
                )
            }

            composable("progreso") {
                PantallaProgreso(logica = logicaProgreso)
            }
            
            composable("subir") {
                PantallaSubirApuntes(
                    logica = logicaEstudio,
                    alTerminar = { id -> 
                        navController.navigate("resultados/$id") {
                            popUpTo("principal")
                        }
                    }
                )
            }
            
            composable(
                route = "resultados/{idApunte}",
                arguments = listOf(navArgument("idApunte") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("idApunte") ?: ""
                PantallaResultados(
                    logica = logicaEstudio,
                    idApunte = id,
                    alIrATest = { navController.navigate("test") }
                )
            }
            
            composable("test") {
                PantallaTestInteractivo(
                    logica = logicaEstudio,
                    alTerminar = { navController.navigate("principal") }
                )
            }
        }
    }
}