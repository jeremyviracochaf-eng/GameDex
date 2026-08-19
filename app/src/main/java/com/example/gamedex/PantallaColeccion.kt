package com.example.gamedex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamedex.ui.GamerTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaColeccion(
    navController: NavController,
    viewModel: JuegosViewModel,
) {
    // Escuchamos la base de datos local en tiempo real
    val juegosGuardados by viewModel.todosLosJuegosLocales.collectAsState()

    // Control de las pestañas (Tabs)
    val estados = listOf("Pendiente", "Jugando", "Terminado")
    var estadoSeleccionado by remember { mutableIntStateOf(0) }

    // Filtramos la lista automáticamente según la pestaña activa
    val juegosFiltrados = juegosGuardados.filter {
        it.estadoJuego == estados[estadoSeleccionado]
    }

    Scaffold(
        topBar = {
            GamerTopAppBar(
                title = "MI BACKLOG",
                actions = {
                    IconButton(onClick = { navController.navigate("escaner_qr") }) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. La barra de navegación de estados
            TabRow(
                selectedTabIndex = estadoSeleccionado,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[estadoSeleccionado]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                estados.forEachIndexed { index, estado ->
                    Tab(
                        selected = estadoSeleccionado == index,
                        onClick = { estadoSeleccionado = index },
                        text = {
                            Text(
                                text = estado.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (estadoSeleccionado == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 2. El contenido de la lista
            if (juegosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No hay juegos aquí todavía.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(juegosFiltrados) { juego ->
                        ItemJuegoLocal(
                            juego = juego,
                            alHacerClic = { navController.navigate("detalle/${juego.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemJuegoLocal(juego: Juego, alHacerClic: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { alHacerClic() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = juego.imagenUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = juego.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = juego.desarrollador,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
