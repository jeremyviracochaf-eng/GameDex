package com.example.gamedex

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamedex.ui.GamerSearchBar
import com.example.gamedex.ui.GamerTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExplorar(
    navController: NavController,
    viewModel: JuegosViewModel,
) {
    val juegosApi by viewModel.juegosInternet.collectAsState()
    val estaCargando by viewModel.estaCargandoApi.collectAsState()
    val contexto = LocalContext.current

    // ---------------------------------------------------------
    // BUSCADOR
    // ---------------------------------------------------------

    var textoBusqueda by remember {
        mutableStateOf("")
    }

    val juegosFiltrados = if (textoBusqueda.isEmpty()) {
        juegosApi
    } else {
        juegosApi.filter { juego ->
            juego.titulo.contains(
                textoBusqueda,
                ignoreCase = true
            )
        }
    }

    // ---------------------------------------------------------
    // CARGAR JUEGOS DE INTERNET
    // ---------------------------------------------------------

    LaunchedEffect(Unit) {
        if (juegosApi.isEmpty()) {
            viewModel.cargarJuegosInternet()
        }
    }

    // ---------------------------------------------------------
    // PANTALLA
    // ---------------------------------------------------------

    Scaffold(
        topBar = {
            GamerTopAppBar(
                title = "GAMEDEX",
                actions = {
                    IconButton(onClick = { navController.navigate("perfil_gamer") }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = { navController.navigate("ajustes") }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("mi_coleccion") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Mi Backlog", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ---------------------------------------------------------
            // BUSCADOR
            // ---------------------------------------------------------
            GamerSearchBar(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = "Descubre tu próximo juego..."
            )

            // ---------------------------------------------------------
            // LISTA
            // ---------------------------------------------------------
            Box(modifier = Modifier.fillMaxSize()) {
                if (estaCargando && juegosApi.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (juegosFiltrados.isEmpty() && textoBusqueda.isNotEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin resultados para '$textoBusqueda'",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(juegosFiltrados) { juegoRed ->
                            ItemJuegoInternet(
                                juego = juegoRed,
                                alClic = { navController.navigate("detalle/${juegoRed.id}") },
                                alGuardarClic = {
                                    viewModel.guardarJuego(juegoRed.aJuegoLocal())
                                    Toast.makeText(contexto, "${juegoRed.titulo} añadido", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemJuegoInternet(
    juego: JuegoRed,
    alClic: () -> Unit,
    alGuardarClic: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { alClic() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = juego.miniaturaUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = juego.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = juego.desarrollador,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                FilledIconButton(
                    onClick = alGuardarClic,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    }
}
