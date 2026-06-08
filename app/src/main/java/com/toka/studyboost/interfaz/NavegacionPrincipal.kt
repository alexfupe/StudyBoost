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

@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()
    
    val logicaAuth: Autenticacion = viewModel()
    val logicaPrincipal: Principal = viewModel()
    val logicaEstudio: Estudio = viewModel()

    if (!logicaAuth.sesionCargada) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulBrillante)
        }
    } else {
        val destinoInicial = if (logicaAuth.usuarioActual != null) "principal" else "login"
        
        NavHost(navController = navController, startDestination = destinoInicial) {
            composable("login") {
                PantallaInicioSesion(
                    logica = logicaAuth,
                    alIniciarSesion = { navController.navigate("principal") {
                        popUpTo("login") { inclusive = true }
                    }},
                    alIrARegistro = { navController.navigate("registro") }
                )
            }
            
            composable("registro") {
                PantallaRegistro(
                    logica = logicaAuth,
                    alRegistrarse = { navController.navigate("principal") {
                        popUpTo("login") { inclusive = true }
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
                    alIrATest = { navController.navigate("test/$id") },
                    alCerrar = { navController.navigate("principal") {
                        popUpTo("principal") { inclusive = true }
                    }}
                )
            }
            
            composable(
                route = "test/{idApunte}",
                arguments = listOf(navArgument("idApunte") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("idApunte") ?: ""
                PantallaTestInteractivo(
                    logica = logicaEstudio,
                    alTerminar = { aciertos: Int, total: Int ->
                        navController.navigate("resumen_test/$id/$aciertos/$total") {
                            popUpTo("resultados/$id")
                        }
                    }
                )
            }

            composable(
                route = "resumen_test/{idApunte}/{aciertos}/{total}",
                arguments = listOf(
                    navArgument("idApunte") { type = NavType.StringType },
                    navArgument("aciertos") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("idApunte") ?: ""
                val aciertos = backStackEntry.arguments?.getInt("aciertos") ?: 0
                val total = backStackEntry.arguments?.getInt("total") ?: 0
                
                PantallaResumenTest(
                    logica = logicaEstudio,
                    aciertos = aciertos,
                    total = total,
                    alContinuarAExportar = {
                        navController.navigate("resultados/$id") {
                            popUpTo("principal")
                        }
                    }
                )
            }
        }
    }
}
