package com.toka.studyboost.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Estudio

@Composable
fun PantallaTestInteractivo(
    logica: Estudio,
    alTerminar: (Int, Int) -> Unit
) {
    var indicePregunta by remember { mutableIntStateOf(0) }
    var opcionSeleccionada by remember { mutableStateOf<Int?>(null) }
    var aciertos by remember { mutableIntStateOf(0) }
    val respuestasInternas = remember { mutableStateMapOf<Int, Int>() }
    val preguntas = logica.preguntasTest

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulMarinoProfundo
    ) {
        if (preguntas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay preguntas disponibles", color = Blanco)
            }
        } else if (indicePregunta < preguntas.size) {
            val pregunta = preguntas[indicePregunta]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Progreso
                LinearProgressIndicator(
                    progress = { (indicePregunta + 1).toFloat() / preguntas.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = AzulBrillante,
                    trackColor = GrisAzuladoOscuro
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Pregunta ${indicePregunta + 1} de ${preguntas.size}",
                    color = GrisClaro,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = pregunta.enunciado,
                    color = Blanco,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                pregunta.opciones.forEachIndexed { index, opcion ->
                    val esSeleccionada = opcionSeleccionada == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { opcionSeleccionada = index },
                        colors = CardDefaults.cardColors(
                            containerColor = if (esSeleccionada) AzulCobalto.copy(alpha = 0.2f) else GrisAzuladoOscuro
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (esSeleccionada) AzulBrillante else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = opcion,
                            color = Blanco,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val seleccion = opcionSeleccionada ?: return@Button
                        respuestasInternas[indicePregunta] = seleccion

                        // Verificar si es correcta antes de pasar
                        if (seleccion == pregunta.respuestaCorrecta) {
                            aciertos++
                        }

                        if (indicePregunta < preguntas.size - 1) {
                            indicePregunta++
                            opcionSeleccionada = null
                        } else {
                            // Guardamos las respuestas en la lógica antes de terminar
                            logica.respuestasUsuario = respuestasInternas.toMap()
                            alTerminar(aciertos, preguntas.size)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulBrillante),
                    shape = RoundedCornerShape(12.dp),
                    enabled = opcionSeleccionada != null
                ) {
                    Text(
                        text = if (indicePregunta < preguntas.size - 1) "Siguiente" else "Finalizar",
                        color = Blanco,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
