package com.toka.studyboost.interfaz

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Estudio
import java.io.File

/**
 * Pantalla de subida de apuntes.
 *
 * Soporta dos vías de entrada:
 * 1. Selector de archivos del sistema (PDF / TXT).
 * 2. Cámara del dispositivo para escanear apuntes físicos mediante OCR (ML Kit).
 *
 * La lógica de negocio está completamente en [Estudio] — esta pantalla es puramente
 * declarativa y no contiene side effects propios.
 *
 * ## Cámara
 * Usa [ActivityResultContracts.TakePicture] con un URI temporal creado via [FileProvider].
 * Esto es obligatorio a partir de Android 10+ (targetSdk ≥ 29):
 * - `TakePicturePreview` devuelve una miniatura de muy baja resolución y falla en
 *   dispositivos recientes con `targetSdk = 33+`.
 * - `TakePicture` escribe la foto en caché interna (sin permisos extra) y devuelve
 *   el bitmap a plena resolución para un OCR más preciso.
 *
 * El permiso de cámara (`CAMERA`) se solicita en tiempo de ejecución antes de
 * abrir la cámara — solo declararlo en el Manifest no es suficiente desde Android 6.
 */
@Composable
fun PantallaSubirApuntes(
    logica: Estudio,
    alTerminar: (String) -> Unit
) {
    val contexto = LocalContext.current

    // URI temporal donde se guardará la foto antes de pasarla al OCR.
    // Se almacena en caché interna — no necesita permiso de almacenamiento.
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    // —— Launcher 1: selector de archivos PDF/TXT —————————————
    val selectorArchivos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            var nombre: String? = null
            contexto.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) nombre = cursor.getString(nameIndex)
            }
            logica.seleccionarArchivo(uri, nombre ?: "Archivo seleccionado")
        }
    }

    // —— Launcher 2: cámara a plena resolución (TakePicture + FileProvider) ————
    // Devuelve true si la foto se guardó con éxito en fotoUri.
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            val uri = fotoUri ?: return@rememberLauncherForActivityResult
            try {
                // Decodificar el bitmap desde el URI del archivo temporal
                val inputStream = contexto.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    logica.procesarImagenConOCR(bitmap, alTerminar)
                }
            } catch (e: Exception) {
                android.util.Log.e("PantallaSubirApuntes", "Error al leer foto: ${e.message}", e)
            }
        }
    }

    // —— Launcher 3: solicitud de permiso de cámara en tiempo de ejecución —————
    // Después de concedido, lanza la cámara directamente.
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara(contexto, onUri = { uri -> fotoUri = uri }, launcher = launcherCamara)
        }
    }

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
            if (!logica.subiendo) {

                // —— Estado inicial / selección —————————————————
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = AzulBrillante
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Subir Apuntes",
                    color = Blanco,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Selecciona un PDF o escanea tus apuntes con la cámara.",
                    color = GrisClaro,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(48.dp))

                // —— Archivo seleccionado ——————————————————————
                if (logica.uriArchivoSeleccionado != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = AzulBrillante
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = logica.nombreArchivoSeleccionado ?: "Archivo listo",
                                color = Blanco,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { logica.limpiarSeleccion() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar archivo",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                // —— Botón 1: Seleccionar PDF —————————————————
                OutlinedButton(
                    onClick = { selectorArchivos.launch("application/pdf") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulBrillante),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AzulBrillante),
                    shape = RoundedCornerShape(16.dp),
                    enabled = logica.uriArchivoSeleccionado == null
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (logica.uriArchivoSeleccionado == null)
                            "Seleccionar PDF"
                        else
                            "Archivo seleccionado ✓",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // —— Botón 2: Cámara OCR ————————————————————————
                OutlinedButton(
                    onClick = {
                        val permisoOk = ContextCompat.checkSelfPermission(
                            contexto, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (permisoOk) {
                            abrirCamara(
                                contexto,
                                onUri = { uri -> fotoUri = uri },
                                launcher = launcherCamara
                            )
                        } else {
                            launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulBrillante),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AzulBrillante),
                    shape = RoundedCornerShape(16.dp),
                    enabled = logica.uriArchivoSeleccionado == null
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Escanear apuntes físicos",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // —— Mensajes de error ————————————————————————
                if (logica.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = logica.error!!,
                            color = Color(0xFFFF6B6B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // —— Botón: procesar con IA —————————————————
                Button(
                    onClick = { logica.subirApuntes(contexto, alTerminar) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulBrillante,
                        disabledContainerColor = GrisMedio
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = logica.uriArchivoSeleccionado != null
                ) {
                    Text("Procesar con IA", color = Blanco, fontWeight = FontWeight.Bold)
                }

            } else {

                // —— Estado de carga / procesamiento ——————————————
                Text(
                    text = "Analizando con IA...",
                    color = AzulBrillante,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = logica.nombreArchivoSeleccionado ?: "",
                    color = GrisClaro,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                LinearProgressIndicator(
                    progress = { logica.progresoSubida },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = AzulBrillante,
                    trackColor = GrisAzuladoOscuro,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${(logica.progresoSubida * 100).toInt()}% completado",
                    color = Blanco,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        logica.progresoSubida < 0.3f -> "Extrayendo texto..."
                        logica.progresoSubida < 0.85f -> "Gemini está analizando el documento..."
                        else -> "Guardando resultados..."
                    },
                    color = GrisClaro,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Crea un archivo temporal en la caché interna, obtiene su URI via [FileProvider]
 * y lanza el Intent de la cámara apuntando a ese URI.
 *
 * La foto se escribe directamente en caché — no necesita permiso de almacenamiento.
 *
 * @param contexto  Context de la Activity/Composable.
 * @param onUri     Callback que recibe el URI creado (para guardarlo en el estado).
 * @param launcher  Launcher de [ActivityResultContracts.TakePicture].
 */
private fun abrirCamara(
    contexto: android.content.Context,
    onUri: (Uri) -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    try {
        val archivo = File(
            contexto.cacheDir,
            "ocr_foto_${System.currentTimeMillis()}.jpg"
        )
        val uri = FileProvider.getUriForFile(
            contexto,
            "${contexto.packageName}.provider",
            archivo
        )
        onUri(uri)
        launcher.launch(uri)
    } catch (e: Exception) {
        android.util.Log.e("PantallaSubirApuntes", "Error al crear URI de cámara: ${e.message}", e)
    }
}
