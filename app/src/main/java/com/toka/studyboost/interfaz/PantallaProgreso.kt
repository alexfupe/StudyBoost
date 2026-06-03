package com.toka.studyboost.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.toka.studyboost.funciones_pantallas.ProgresoViewModel
import com.toka.studyboost.ui.theme.*

@Composable
fun PantallaProgreso(
    logica: ProgresoViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        logica.cargarEstadisticas()
    }

    val estadisticas by logica.estadisticas.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulMarinoProfundo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Tu Progreso",
                color = Blanco,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            estadisticas?.let { stats ->
                TarjetaEstadistica("Documentos Procesados", stats.totalDocumentos.toString())
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Aciertos Promedio", color = GrisClaro, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { stats.promedioAciertos / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = AzulBrillante,
                    trackColor = GrisAzuladoOscuro,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    text = "${stats.promedioAciertos.toInt()}%",
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(32.dp))
                TarjetaEstadistica("Tiempo de Estudio", "${"%.1f".format(stats.tiempoEstudioHoras)} horas")
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzulBrillante)
            }
        }
    }
}

@Composable
fun TarjetaEstadistica(titulo: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = titulo, color = GrisClaro, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valor, color = Blanco, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
