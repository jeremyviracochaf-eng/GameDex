package com.example.gamedex

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamedex.ui.GamerButton
import com.example.gamedex.ui.GamerTopAppBar
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleJuego(
    navController: NavController,
    viewModel: JuegosViewModel,
    juegoId: Int
) {
    var juegoActual by remember { mutableStateOf<Juego?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogoQR by remember { mutableStateOf(false) }
    var imagenQR by remember { mutableStateOf<Bitmap?>(null) }

    val juegosLocales by viewModel.todosLosJuegosLocales.collectAsState()
    val estaEnBacklog = juegosLocales.any { it.id == juegoId }

    LaunchedEffect(juegoId) {
        cargando = true
        juegoActual = viewModel.obtenerJuegoParaDetalle(juegoId)
        cargando = false
    }

    Scaffold(
        topBar = {
            GamerTopAppBar(
                title = "DETALLES",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (juegoActual == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "No se encontró el juego.")
            }
        } else {
            val juego = juegoActual!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Image
                Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = juego.imagenUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Shadow overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                    startY = 500f
                                )
                            )
                    )
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = juego.titulo,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = juego.desarrollador,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " • ${juego.anioLanzamiento}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Button
                    if (!estaEnBacklog) {
                        GamerButton(
                            text = "AÑADIR AL BACKLOG",
                            onClick = { viewModel.guardarJuego(juego.copy(estadoJuego = "Pendiente")) },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Add
                        )
                    } else {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "EN TU COLECCIÓN",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status selection
                    Text(
                        text = "ESTADO ACTUAL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val estados = listOf("Pendiente", "Jugando", "Terminado")
                        estados.forEach { estado ->
                            val seleccionado = juego.estadoJuego == estado
                            BotonEstadoMejorado(
                                texto = estado,
                                estaSeleccionado = seleccionado,
                                modifier = Modifier.weight(1f)
                            ) {
                                viewModel.actualizarEstadoJuego(juego, estado)
                                juegoActual = juego.copy(estadoJuego = estado)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Synopsis
                    Text(
                        text = "SINOPSIS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = juego.sinopsis,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // QR Action
                    OutlinedButton(
                        onClick = {
                            val jsonJuego = Gson().toJson(juego.copy(estadoJuego = "Pendiente"))
                            imagenQR = generarCodigoQR(jsonJuego)
                            mostrarDialogoQR = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPARTIR JUEGO (QR)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (mostrarDialogoQR && imagenQR != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoQR = false },
            title = { Text("Código QR", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Escanea este código para compartir.")
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        bitmap = imagenQR!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(250.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = juegoActual?.titulo ?: "", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoQR = false }) { Text("CERRAR") }
            }
        )
    }
}

private fun generarCodigoQR(contenido: String): Bitmap {
    val size = 800
    val matriz = MultiFormatWriter().encode(contenido, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matriz[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}

@Composable
fun BotonEstadoMejorado(
    texto: String,
    estaSeleccionado: Boolean,
    modifier: Modifier = Modifier,
    alHacerClic: () -> Unit
) {
    Surface(
        onClick = alHacerClic,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (estaSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!estaSeleccionado) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal,
                color = if (estaSeleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

