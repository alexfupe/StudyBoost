package com.toka.studyboost.interfaz

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.R
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Autenticacion

@Composable
fun PantallaInicioSesion(
    logica: Autenticacion,
    alIniciarSesion: () -> Unit,
    alIrARegistro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulMarinoProfundo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // —— Logo y Nombre de la App ————————————————————
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = AzulBrillante
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "StudyBoost",
                color = Blanco,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Iniciar Sesión",
                color = GrisClaro,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = GrisClaro) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GrisAzuladoOscuro,
                    unfocusedContainerColor = GrisAzuladoOscuro,
                    focusedTextColor = Blanco,
                    unfocusedTextColor = Blanco,
                    cursorColor = AzulBrillante,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña", color = GrisClaro) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = GrisClaro)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GrisAzuladoOscuro,
                    unfocusedContainerColor = GrisAzuladoOscuro,
                    focusedTextColor = Blanco,
                    unfocusedTextColor = Blanco,
                    cursorColor = AzulBrillante,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { logica.iniciarSesion(email, contrasena, alIniciarSesion) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulBrillante),
                shape = RoundedCornerShape(12.dp),
                enabled = !logica.cargando
            ) {
                if (logica.cargando) {
                    CircularProgressIndicator(color = Blanco, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", color = Blanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(text = "¿No tienes cuenta? ", color = GrisClaro)
                Text(
                    text = "Regístrate",
                    color = AzulBrillante,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { alIrARegistro() }
                )
            }

            logica.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}
