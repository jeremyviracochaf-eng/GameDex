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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaColeccion(
    navController: NavController,
    viewModel: JuegosViewModel
) {
    // Escuchamos la base de datos local en tiempo real
    val juegosGuardados by viewModel.todosLosJuegosLocales.collectAsState()

    // Control de las pestañas (Tabs)
    val estados = listOf("Pendiente", "Jugando", "Terminado")
    var estadoSeleccionado by remember { mutableStateOf(0) }

    // Filtramos la lista automáticamente según la pestaña activa
    val juegosFiltrados = juegosGuardados.filter {
        it.estadoJuego == estados[estadoSeleccionado]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Backlog", fontWeight = FontWeight.Bold) },
                actions = {
                    // Botón para la funcionalidad de Hardware (Escáner QR)
                    IconButton(onClick = { navController.navigate("escaner_qr") }) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. La barra de navegación de estados
            TabRow(selectedTabIndex = estadoSeleccionado) {
                estados.forEachIndexed { index, estado ->
                    Tab(
                        selected = estadoSeleccionado == index,
                        onClick = { estadoSeleccionado = index },
                        text = { Text(estado, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // 2. El contenido de la lista
            if (juegosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay juegos en esta categoría.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(juegosFiltrados) { juego ->
                        ItemJuegoLocal(
                            juego = juego,
                            alHacerClic = {
                                // Navegamos a la pantalla de detalles pasando el ID del juego
                                navController.navigate("detalle/${juego.id}")
                            }
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
            .height(110.dp)
            .clickable { alHacerClic() }, // Hacemos que toda la tarjeta sea clickeable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = juego.imagenUrl,
                contentDescription = "Portada de ${juego.titulo}",
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = juego.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = juego.desarrollador,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}