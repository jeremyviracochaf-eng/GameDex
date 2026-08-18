package com.example.gamedex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleJuego(
    navController: NavController,
    viewModel: JuegosViewModel,
    juegoId: Int
) {
    // Estado para guardar el juego que encontramos en la base de datos
    var juegoActual by remember { mutableStateOf<Juego?>(null) }

    // Buscamos el juego en Room apenas se abre la pantalla
    LaunchedEffect(key1 = juegoId) {
        juegoActual = viewModel.obtenerJuegoPorId(juegoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Juego") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Si el juego aún se está cargando, mostramos un indicador
        if (juegoActual == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Desempaquetamos de forma segura
            val juego = juegoActual!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // Permite hacer scroll si el texto es muy largo
            ) {
                // 1. Imagen de Portada
                AsyncImage(
                    model = juego.imagenUrl,
                    contentDescription = "Imagen de ${juego.titulo}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    // 2. Título y Año
                    Text(
                        text = juego.titulo,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Desarrollado por ${juego.desarrollador} • ${juego.anioLanzamiento}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Controles para cambiar el estado (El Backlog)
                    Text(text = "Estado en tu Colección", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BotonEstado(
                            texto = "Pendiente",
                            estaSeleccionado = juego.estadoJuego == "Pendiente",
                            alHacerClic = {
                                viewModel.actualizarEstadoJuego(juego, "Pendiente")
                                juegoActual = juego.copy(estadoJuego = "Pendiente") // Actualiza la UI
                            }
                        )
                        BotonEstado(
                            texto = "Jugando",
                            estaSeleccionado = juego.estadoJuego == "Jugando",
                            alHacerClic = {
                                viewModel.actualizarEstadoJuego(juego, "Jugando")
                                juegoActual = juego.copy(estadoJuego = "Jugando")
                            }
                        )
                        BotonEstado(
                            texto = "Terminado",
                            estaSeleccionado = juego.estadoJuego == "Terminado",
                            alHacerClic = {
                                viewModel.actualizarEstadoJuego(juego, "Terminado")
                                juegoActual = juego.copy(estadoJuego = "Terminado")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Sinopsis
                    Text(text = "Sinopsis", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = juego.sinopsis,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

// Componente visual reutilizable para los botones de estado
@Composable
fun BotonEstado(texto: String, estaSeleccionado: Boolean, alHacerClic: () -> Unit) {
    Button(
        onClick = alHacerClic,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (estaSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (estaSeleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (estaSeleccionado) 4.dp else 0.dp)
    ) {
        Text(text = texto, fontWeight = FontWeight.SemiBold)
    }
}