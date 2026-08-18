package com.example.gamedex

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExplorar(
    navController: NavController,
    viewModel: JuegosViewModel
) {
    val juegosApi by viewModel.juegosInternet.collectAsState()
    val estaCargando by viewModel.estaCargandoApi.collectAsState()
    val contexto = LocalContext.current

    // Ejecuta la llamada a internet solo si la lista está vacía
    LaunchedEffect(Unit) {
        if (juegosApi.isEmpty()) {
            viewModel.cargarJuegosInternet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Descubrir Juegos", fontWeight = FontWeight.Bold) },
                actions = {
                    // Botón para ir a los ajustes (DataStore)
                    IconButton(onClick = { navController.navigate("ajustes") }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // Botón flotante para ir a tu colección local
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("mi_coleccion") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Mi Colección") },
                text = { Text("Ver Mi Backlog") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (estaCargando && juegosApi.isEmpty()) {
                // Indicador de carga mientras Retrofit hace su trabajo
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(juegosApi) { juegoRed ->
                        ItemJuegoInternet(
                            juego = juegoRed,
                            alGuardarClic = {
                                // Convertimos el juego de la API al modelo de Room y lo guardamos
                                viewModel.guardarJuego(juegoRed.aJuegoLocal())
                                Toast.makeText(contexto, "${juegoRed.titulo} guardado en tu colección", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio para que el FAB no tape el último elemento
                }
            }
        }
    }
}

@Composable
fun ItemJuegoInternet(
    juego: JuegoRed,
    alGuardarClic: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen cargada con Coil desde internet
            AsyncImage(
                model = juego.miniaturaUrl,
                contentDescription = "Portada de ${juego.titulo}",
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = juego.desarrollador,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Botón para agregar a Room
            IconButton(
                onClick = alGuardarClic,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Guardar juego",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}