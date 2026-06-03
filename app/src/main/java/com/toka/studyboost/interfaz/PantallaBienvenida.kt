package com.toka.studyboost.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.ui.theme.AzulBrillante
import com.toka.studyboost.ui.theme.AzulMarinoProfundo
import com.toka.studyboost.ui.theme.Blanco
import com.toka.studyboost.ui.theme.GrisClaro

@Composable
fun PantallaBienvenida(alContinuar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulMarinoProfundo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de Graduación (Birrete)
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Icono de Estudio",
                modifier = Modifier.size(140.dp),
                tint = AzulBrillante
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Bienvenido a StudyBoost",
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Acelera tu aprendizaje con Inteligencia Artificial. Genera resúmenes y tests de tus apuntes en segundos.",
                color = GrisClaro,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Botón Continuar
            Button(
                onClick = alContinuar,
                colors = ButtonDefaults.buttonColors(containerColor = AzulBrillante),
                modifier = Modifier.size(72.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continuar",
                    tint = Blanco,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
