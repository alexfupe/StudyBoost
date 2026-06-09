package com.toka.studyboost.interfaz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Autenticacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    logica: Autenticacion,
    alVolver: () -> Unit
) {
    val usuario = logica.usuarioActual
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var passActualVisible by remember { mutableStateOf(false) }
    var passNuevaVisible by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMarinoProfundo)
            )
        },
        containerColor = AzulMarinoProfundo
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera de usuario
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AzulBrillante
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = usuario?.nombre ?: "Usuario",
                color = Blanco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = usuario?.email ?: "",
                color = GrisClaro,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Sección de cambio de contraseña
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AzulBrillante)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiar Contraseña", color = Blanco, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = passActual,
                        onValueChange = { passActual = it },
                        label = { Text("Contraseña Actual", color = GrisClaro) },
                        visualTransformation = if (passActualVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passActualVisible = !passActualVisible }) {
                                Icon(
                                    imageVector = if (passActualVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = GrisClaro
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AzulMarinoProfundo,
                            unfocusedContainerColor = AzulMarinoProfundo,
                            focusedTextColor = Blanco,
                            unfocusedTextColor = Blanco
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = passNueva,
                        onValueChange = { passNueva = it },
                        label = { Text("Nueva Contraseña", color = GrisClaro) },
                        visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passNuevaVisible = !passNuevaVisible }) {
                                Icon(
                                    imageVector = if (passNuevaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = GrisClaro
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AzulMarinoProfundo,
                            unfocusedContainerColor = AzulMarinoProfundo,
                            focusedTextColor = Blanco,
                            unfocusedTextColor = Blanco
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            logica.cambiarContrasena(passActual, passNueva) {
                                mostrarDialogo = true
                                passActual = ""
                                passNueva = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulBrillante),
                        enabled = passActual.isNotEmpty() && passNueva.isNotEmpty() && !logica.cargando
                    ) {
                        if (logica.cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Blanco)
                        } else {
                            Text("Actualizar Contraseña", color = Blanco)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Éxito") },
            text = { Text("Tu contraseña ha sido actualizada correctamente.") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
