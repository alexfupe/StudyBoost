package com.toka.studyboost.interfaz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Autenticacion

@Composable
fun PantallaRegistro(
    logica: Autenticacion,
    alRegistrarse: () -> Unit,
    alVolverALogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

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
            Text(
                text = "Crear Cuenta",
                color = Blanco,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(48.dp))

            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo", color = GrisClaro) },
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
                visualTransformation = PasswordVisualTransformation(),
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
                onClick = { logica.registrarse(nombre, email, contrasena, alRegistrarse) },
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
                    Text("Registrarse", color = Blanco, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(text = "¿Ya tienes cuenta? ", color = GrisClaro)
                Text(
                    text = "Inicia Sesión",
                    color = AzulBrillante,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { alVolverALogin() }
                )
            }

            logica.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}
