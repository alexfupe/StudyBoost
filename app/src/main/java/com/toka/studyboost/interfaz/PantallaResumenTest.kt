package com.toka.studyboost.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Estudio

@Composable
fun PantallaResumenTest(
    logica: Estudio,
    aciertos: Int,
    total: Int,
    alContinuarAExportar: () -> Unit,
    alVolverAlInicio: () -> Unit
) {
    val contexto = LocalContext.current
    var guardadoExitoso by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulMarinoProfundo
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // —— Cabecera de puntuación ————————————————————
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GrisAzuladoOscuro)
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "¡Buen trabajo!",
                            color = AzulBrillante,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Test Completado",
                            color = Blanco,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Círculo de puntuación
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(AzulCobalto.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$aciertos/$total",
                                    color = Blanco,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "aciertos",
                                    color = GrisClaro,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // —— Botones de Acción ————————————————————————
            item {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            logica.sesionActual?.id?.let { id ->
                                logica.guardarResultadoTest(id, aciertos, total)
                                guardadoExitoso = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (guardadoExitoso) Color(0xFF10B981) else AzulBrillante
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (guardadoExitoso) Icons.Default.CheckCircle else Icons.Default.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (guardadoExitoso) "¡Guardado en Historial!" else "Guardar Resultados",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            logica.exportarComoPDF()?.let {
                                contexto.startActivity(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrisAzuladoOscuro),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AzulBrillante)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Exportar Resultados a PDF", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(
                        onClick = alVolverAlInicio,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = GrisClaro)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Volver al Inicio", color = GrisClaro, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // —— Título de Revisión ————————————————————————
            item {
                Text(
                    text = "Revisión Detallada",
                    color = Blanco,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // —— Lista de Preguntas ————————————————————————
            itemsIndexed(logica.preguntasTest) { index, pregunta ->
                val respuestaUsuario = logica.respuestasUsuario[index]
                val esCorrecta = respuestaUsuario == pregunta.respuestaCorrecta

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (esCorrecta) Color(0xFF10B981) else Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (esCorrecta) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Blanco,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = pregunta.enunciado,
                                color = Blanco,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Respuesta del usuario
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AzulMarinoProfundo.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Tu respuesta:",
                                color = GrisClaro,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pregunta.opciones.getOrNull(respuestaUsuario ?: -1) ?: "Sin responder",
                                color = if (esCorrecta) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 14.sp
                            )
                        }

                        if (!esCorrecta) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Respuesta correcta:",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pregunta.opciones.getOrNull(pregunta.respuestaCorrecta) ?: "",
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
