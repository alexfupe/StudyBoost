package com.toka.studyboost.interfaz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toka.studyboost.datos.Apunte
import com.toka.studyboost.ui.theme.*
import com.toka.studyboost.funciones_pantallas.Principal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    logica: Principal,
    nombreUsuario: String,
    alSubirApuntes: () -> Unit,
    alVerDocumento: (String) -> Unit,
    alIrAPerfil: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Observar el StateFlow de Room — se actualiza automáticamente al insertar sesiones
    val apuntesFiltrados by logica.sesionesComoApuntes.collectAsStateWithLifecycle()

    // Callbacks recordados para evitar recomposiciones innecesarias
    val onSubirClick = remember { alSubirApuntes }
    val onPerfilClick = remember { alIrAPerfil }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = GrisPizarraOscuro,
                drawerContentColor = Blanco
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AzulCobalto),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombreUsuario.take(1).uppercase(),
                            color = Blanco,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = nombreUsuario, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Estudiante Premium", color = AzulBrillante, fontSize = 14.sp)
                }
                
                HorizontalDivider(color = GrisAzuladoOscuro, modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationDrawerItem(
                    label = { Text("Mis Apuntes") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = AzulBrillante.copy(alpha = 0.2f),
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = AzulBrillante,
                        unselectedTextColor = Blanco,
                        selectedIconColor = AzulBrillante,
                        unselectedIconColor = Blanco
                    )
                )

                DrawerMenuItem("Mi Perfil", Icons.Default.Person) {
                    scope.launch {
                        drawerState.close()
                        alIrAPerfil()
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                DrawerMenuItem("Cerrar Sesión", Icons.AutoMirrored.Filled.ExitToApp, color = Color.Red) {
                    scope.launch {
                        drawerState.close()
                        alCerrarSesion()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("StudyBoost", color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Hola, $nombreUsuario", color = GrisClaro, fontSize = 12.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Blanco)
                        }
                    },
                    actions = {
                        // Círculo con inicial del nombre (puramente visual, no interactivo)
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AzulCobalto),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nombreUsuario.take(1).uppercase(),
                                color = Blanco,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMarinoProfundo)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onSubirClick,
                    containerColor = AzulBrillante,
                    contentColor = Blanco,
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                    text = { Text("Subir Apuntes") }
                )
            },
            containerColor = AzulMarinoProfundo
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Barra de búsqueda optimizada
                TextField(
                    value = logica.textoBusqueda,
                    onValueChange = { logica.textoBusqueda = it },
                    placeholder = { Text("Buscar en mis apuntes...", color = GrisClaro) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrisClaro) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GrisAzuladoOscuro,
                        unfocusedContainerColor = GrisAzuladoOscuro,
                        focusedTextColor = Blanco,
                        unfocusedTextColor = Blanco,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // Resumen de estadísticas rápidas
                Text(
                    text = "Resumen",
                    color = Blanco,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaMiniResumen("Apuntes", logica.totalApuntes, Icons.Default.AutoStories, AzulBrillante)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaMiniResumen("Tests", logica.totalTests, Icons.Default.Quiz, Color(0xFF10B981))
                    }
                }

                Text(
                    text = "Documentos Recientes",
                    color = Blanco,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // La lista ya viene filtrada del ViewModel con derivedStateOf
                val listaFiltrada = logica.apuntesFiltrados

                if (logica.cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulBrillante)
                    }
                } else if (listaFiltrada.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No hay documentos todavía", color = GrisClaro)
                            Text(
                                "Sube tu primer apunte con el botón +",
                                color = GrisClaro.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = listaFiltrada,
                            key = { it.id } // Key estable para animaciones de lista
                        ) { apunte ->
                            TarjetaApunteMejorada(
                                apunte = apunte,
                                onClick = { alVerDocumento(apunte.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(texto: String, icono: ImageVector, color: Color = Blanco, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(texto) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icono, contentDescription = null, tint = color) },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedTextColor = color,
            unselectedIconColor = color,
            unselectedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun TarjetaMiniResumen(titulo: String, valor: String, icono: ImageVector, colorIcono: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GrisAzuladoOscuro),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = valor, color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = titulo, color = GrisClaro, fontSize = 12.sp)
        }
    }
}

@Composable
fun TarjetaApunteMejorada(apunte: Apunte, onClick: () -> Unit) {
    val tieneTest = apunte.fecha.contains("| Test:")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (tieneTest) AzulCobalto.copy(alpha = 0.15f) else GrisAzuladoOscuro
        ),
        shape = RoundedCornerShape(20.dp),
        border = if (tieneTest) BorderStroke(1.dp, AzulBrillante.copy(alpha = 0.5f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (tieneTest) AzulBrillante.copy(alpha = 0.2f) 
                        else AzulCobalto.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tieneTest) Icons.Default.Quiz else Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = AzulBrillante,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apunte.titulo,
                    color = Blanco,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (tieneTest) Icons.Default.CheckCircle else Icons.Default.CalendarToday, 
                        contentDescription = null, 
                        tint = if (tieneTest) Color(0xFF10B981) else GrisClaro, 
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = apunte.fecha,
                        color = if (tieneTest) Blanco else GrisClaro,
                        fontSize = 12.sp,
                        fontWeight = if (tieneTest) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrisClaro
            )
        }
    }
}
