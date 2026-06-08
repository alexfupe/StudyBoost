package com.toka.studyboost.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Estudio

/**
 * Pantalla de resultados — muestra el resumen y las preguntas generadas por la IA.
 *
 * Nuevas funcionalidades:
 * - Tab "Exportar": permite compartir como Markdown o PDF.
 * - Carga desde Room si la sesión no está en memoria (modo offline).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaResultados(
    logica: Estudio,
    idApunte: String,
    alIrATest: () -> Unit,
    alCerrar: () -> Unit
) {
    val contexto = LocalContext.current
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("Resumen", "Preguntas", "Exportar")

    LaunchedEffect(idApunte) {
        logica.cargarResultados(idApunte)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(logica.sesionActual?.titulo ?: "Resultados", color = Blanco) },
                actions = {
                    TextButton(onClick = alCerrar) {
                        Text("Cerrar", color = AzulBrillante, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMarinoProfundo)
            )
        },
        containerColor = AzulMarinoProfundo
    ) { padding ->
        if (logica.cargandoResultados) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzulBrillante)
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                // —— Barra de tabs ———————————————————————
                TabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = GrisAzuladoOscuro,
                    contentColor = AzulBrillante,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                            color = AzulBrillante
                        )
                    }
                ) {
                    titulos.forEachIndexed { index, titulo ->
                        Tab(
                            selected = tabSeleccionada == index,
                            onClick = { tabSeleccionada = index },
                            text = { Text(titulo, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (tabSeleccionada) {

                        // —— Tab 1: Resumen —————————————————
                        0 -> {
                            Text(
                                text = "Resumen Generado",
                                color = Blanco,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = logica.resumenActual?.texto ?: "No hay resumen disponible.",
                                    color = Blanco,
                                    modifier = Modifier.padding(16.dp),
                                    lineHeight = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = alIrATest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulBrillante),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Ir al Test Interactivo",
                                    color = Blanco,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // —— Tab 2: Preguntas —————————————————
                        1 -> {
                            Text(
                                text = "Preguntas Sugeridas",
                                color = Blanco,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            logica.preguntasTest.forEachIndexed { i, pregunta ->
                                Card(
                                    modifier = Modifier
                                        .padding(bottom = 12.dp)
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${i + 1}. ${pregunta.enunciado}",
                                            color = Blanco,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        pregunta.opciones.forEachIndexed { _, opcion ->
                                            Text(
                                                text = "  ○ $opcion",
                                                color = GrisClaro,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // —— Tab 3: Exportar ——————————————————
                        2 -> {
                            Text(
                                text = "Exportar Resultados",
                                color = Blanco,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Comparte el resumen y las preguntas en el formato que prefieras.",
                                color = GrisClaro,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // Botón Markdown
                            Button(
                                onClick = {
                                    logica.exportarComoMarkdown()?.let {
                                        contexto.startActivity(it)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GrisAzuladoOscuro),
                                shape = RoundedCornerShape(16.dp),
                                enabled = logica.sesionActual != null
                            ) {
                                Icon(
                                    Icons.Default.IosShare,
                                    contentDescription = null,
                                    tint = AzulBrillante,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Compartir como Markdown",
                                        color = Blanco,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Compatible con Notion, Obsidian, GitHub",
                                        color = GrisClaro,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botón PDF
                            Button(
                                onClick = {
                                    logica.exportarComoPDF()?.let {
                                        contexto.startActivity(it)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GrisAzuladoOscuro),
                                shape = RoundedCornerShape(16.dp),
                                enabled = logica.sesionActual != null
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = AzulBrillante,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Compartir como PDF",
                                        color = Blanco,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Abre el selector de apps del sistema",
                                        color = GrisClaro,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (logica.sesionActual == null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Primero procesa un documento para habilitar la exportación.",
                                    color = GrisClaro,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
