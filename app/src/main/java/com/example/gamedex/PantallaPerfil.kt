package com.example.gamedex

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gamedex.ui.GamerButton
import com.example.gamedex.ui.GamerTopAppBar
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    navController: NavController,
    viewModel: JuegosViewModel,
) {
    val contexto = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // =========================================================
    // DATOS DEL PERFIL - DATASTORE
    // =========================================================

    val gamertagGuardado by viewModel.gamertag.collectAsState()
    val fraseGuardada by viewModel.frase.collectAsState()
    val avatarUriStr by viewModel.avatarUri.collectAsState()

    // Estados temporales de los campos de texto
    var gamertagTexto by remember(gamertagGuardado) {
        mutableStateOf(gamertagGuardado)
    }

    var fraseTexto by remember(fraseGuardada) {
        mutableStateOf(fraseGuardada)
    }

    // =========================================================
    // ESTADÍSTICAS - ROOM
    // =========================================================

    val totalJuegos by viewModel.totalJuegos.collectAsState()
    val terminados by viewModel.juegosTerminados.collectAsState()
    val jugando by viewModel.juegosJugando.collectAsState()
    val pendientes by viewModel.juegosPendientes.collectAsState()

    // =========================================================
    // URI TEMPORAL DE LA CÁMARA
    // =========================================================

    var uriFotoTemporal by remember {
        mutableStateOf<Uri?>(null)
    }

    // =========================================================
    // COPIAR IMAGEN DE GALERÍA A INTERNO (Para persistencia)
    // =========================================================

    fun copiarImagenAInterno(uri: Uri): String? {
        return try {
            val inputStream = contexto.contentResolver.openInputStream(uri) ?: return null
            val archivoDestino = File(contexto.filesDir, "avatar_galeria.jpg")
            archivoDestino.outputStream().use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            archivoDestino.toUri().toString()
        } catch (e: Exception) {
            null
        }
    }

    // =========================================================
    // GALERÍA - PHOTO PICKER MODERNO
    // =========================================================

    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val uriInterna = copiarImagenAInterno(it)
            if (uriInterna != null) {
                viewModel.guardarPerfil(
                    gamertag = gamertagTexto,
                    frase = fraseTexto,
                    avatarUri = uriInterna
                )
            }
        }
    }

    // =========================================================
    // CÁMARA
    // =========================================================

    val tomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exitoso ->
        val uri = uriFotoTemporal
        if (exitoso && uri != null) {
            viewModel.guardarPerfil(
                gamertag = gamertagTexto,
                frase = fraseTexto,
                avatarUri = uri.toString()
            )
        }
        uriFotoTemporal = null
    }

    // =========================================================
    // PERMISO DE CÁMARA
    // =========================================================

    val permisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->

        if (concedido) {

            // Si el usuario acaba de conceder el permiso,
            // continuamos con la cámara.
            val uri = uriFotoTemporal

            if (uri != null) {
                tomarFoto.launch(uri)
            }

        } else {

            // Si rechazó el permiso, eliminamos la URI temporal.
            val uri = uriFotoTemporal

            if (uri != null) {
                try {
                    contexto.contentResolver.delete(
                        uri,
                        null,
                        null
                    )
                } catch (_: Exception) {
                }
            }

            uriFotoTemporal = null
        }
    }

    // =========================================================
    // CREAR URI PARA LA FOTO
    // =========================================================

    fun crearUriParaCamara(): Uri? {
        return try {
            // Guardamos en filesDir para que persista
            val archivo = File(
                contexto.filesDir,
                "avatar_foto.jpg"
            )

            FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                archivo
            )
        } catch (e: Exception) {
            null
        }
    }

    // =========================================================
    // ABRIR CÁMARA
    // =========================================================

    fun abrirCamara() {

        val nuevoUri = crearUriParaCamara()

        if (nuevoUri == null) {
            return
        }

        uriFotoTemporal = nuevoUri

        val tienePermiso =
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {

            tomarFoto.launch(nuevoUri)

        } else {

            permisoCamara.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // =========================================================
    // INTERFAZ
    // =========================================================

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamerTopAppBar(
                title = "PERFIL",
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Avatar Section
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUriStr.isNotEmpty()) {
                        AsyncImage(
                            model = avatarUriStr.toUri(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(48.dp),
                    onClick = { abrirCamara() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CÁMARA")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f).height(48.dp),
                    onClick = { selectorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GALERÍA")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            OutlinedTextField(
                value = gamertagTexto,
                onValueChange = { gamertagTexto = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gamertag") },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fraseTexto,
                onValueChange = { fraseTexto = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Frase gamer") },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            GamerButton(
                text = "GUARDAR CAMBIOS",
                onClick = {
                    viewModel.guardarPerfil(gamertagTexto.trim(), fraseTexto.trim(), avatarUriStr)
                    scope.launch { snackbarHostState.showSnackbar("¡Perfil actualizado!") }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "ESTADÍSTICAS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ItemEstadisticaMejorada("TOTAL", totalJuegos.toString(), Modifier.weight(1f))
                        ItemEstadisticaMejorada("LISTOS", terminados.toString(), Modifier.weight(1f))
                        ItemEstadisticaMejorada("JUGANDO", jugando.toString(), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ItemEstadisticaMejorada(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(text = titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
