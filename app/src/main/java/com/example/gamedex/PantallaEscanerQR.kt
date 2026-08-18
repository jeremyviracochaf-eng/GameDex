package com.example.gamedex

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEscanerQR(
    navController: NavController,
    viewModel: JuegosViewModel // Necesitamos el ViewModel para guardar el juego escaneado
) {
    val contexto = LocalContext.current

    // Estados para controlar los permisos
    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permisoDenegado by remember { mutableStateOf(false) }

    // El lanzador que muestra el cuadro de diálogo del sistema preguntando "Permitir / Denegar"
    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { esConcedido ->
            permisoConcedido = esConcedido
            permisoDenegado = !esConcedido
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escáner de Juegos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (permisoConcedido) {
                // EL PERMISO FUE APROBADO: Mostramos la interfaz de la cámara/escáner
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cámara lista",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cámara autorizada y lista para leer el código QR de tu juego físico.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        // Simulamos que el QR contenía la información de un juego nuevo
                        val juegoEscaneado = Juego(
                            id = (1000..9999).random(), // ID aleatorio para simular
                            titulo = "Zelda: Ocarina of Time (Escaneado)",
                            desarrollador = "Nintendo",
                            anioLanzamiento = 1998,
                            sinopsis = "Juego añadido físicamente mediante código QR.",
                            imagenUrl = "https://www.freetogame.com/g/1/thumbnail.jpg", // Placeholder
                            metacritic = 99,
                            estadoJuego = "Pendiente"
                        )
                        viewModel.guardarJuego(juegoEscaneado)
                        Toast.makeText(contexto, "¡Juego escaneado y guardado en tu colección!", Toast.LENGTH_LONG).show()
                        navController.popBackStack() // Volvemos a la colección
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Simular Escaneo de QR", fontWeight = FontWeight.Bold)
                }

            } else {
                // EL PERMISO AÚN NO SE PIDE O FUE RECHAZADO
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Se requiere cámara",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (permisoDenegado) {
                    // Manejo del caso en que el usuario lo rechace (Requisito del sílabo)
                    Text(
                        text = "Has denegado el permiso de la cámara. Esta función es indispensable para escanear tus juegos físicos. Por favor, otórgalo para continuar.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Necesitamos acceso a tu cámara para escanear el código QR de los juegos y añadirlos a tu Backlog.",
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Otorgar Permiso de Cámara", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}