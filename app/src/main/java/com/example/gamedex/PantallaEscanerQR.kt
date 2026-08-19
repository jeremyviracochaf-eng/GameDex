package com.example.gamedex

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.gamedex.ui.GamerTopAppBar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEscanerQR(
    navController: NavController,
    viewModel: JuegosViewModel
) {
    val contexto = LocalContext.current
    val cicloDeVida = LocalLifecycleOwner.current

    // =========================================================
    // PERMISO DE CÁMARA
    // =========================================================

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
    }

    // =========================================================
    // CONTROL DE ESCANEO
    // =========================================================

    val escaneoEnProceso = remember {
        AtomicBoolean(false)
    }

    Scaffold(
        topBar = {
            GamerTopAppBar(
                title = "ESCANEAR QR",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (permisoConcedido) {

                // =================================================
                // CÁMARA
                // =================================================

                AndroidView(

                    factory = { ctx ->

                        val previewView =
                            PreviewView(ctx)

                        val cameraProviderFuture =
                            ProcessCameraProvider
                                .getInstance(ctx)

                        cameraProviderFuture.addListener(

                            {

                                try {

                                    val cameraProvider =
                                        cameraProviderFuture.get()

                                    // ---------------------------------
                                    // PREVIEW
                                    // ---------------------------------

                                    val preview =
                                        Preview.Builder()
                                            .build()
                                            .also {
                                                it.setSurfaceProvider(
                                                    previewView
                                                        .surfaceProvider
                                                )
                                            }

                                    // ---------------------------------
                                    // CONFIGURACIÓN ML KIT
                                    // ---------------------------------

                                    val opciones =
                                        BarcodeScannerOptions
                                            .Builder()
                                            .setBarcodeFormats(
                                                Barcode.FORMAT_QR_CODE
                                            )
                                            .build()

                                    val scanner =
                                        BarcodeScanning
                                            .getClient(opciones)

                                    // ---------------------------------
                                    // ANALIZADOR
                                    // ---------------------------------

                                    val imageAnalysis =
                                        ImageAnalysis.Builder()
                                            .setBackpressureStrategy(
                                                ImageAnalysis
                                                    .STRATEGY_KEEP_ONLY_LATEST
                                            )
                                            .build()

                                    val executor =
                                        Executors.newSingleThreadExecutor()

                                    imageAnalysis.setAnalyzer(
                                        executor
                                    ) { imageProxy ->

                                        procesarImagenQR(
                                            imageProxy = imageProxy,
                                            scanner = scanner
                                        ) { valorLeido ->

                                            // Evita procesar el mismo QR
                                            // muchas veces
                                            if (
                                                escaneoEnProceso
                                                    .compareAndSet(
                                                        false,
                                                        true
                                                    )
                                            ) {

                                                procesarQR(
                                                    valorLeido =
                                                        valorLeido,
                                                    viewModel =
                                                        viewModel,
                                                    contexto =
                                                        ctx,
                                                    navController =
                                                        navController,
                                                    alFallar = {

                                                        escaneoEnProceso
                                                            .set(false)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // ---------------------------------
                                    // CÁMARA TRASERA
                                    // ---------------------------------

                                    val cameraSelector =
                                        CameraSelector
                                            .DEFAULT_BACK_CAMERA

                                    cameraProvider.unbindAll()

                                    cameraProvider.bindToLifecycle(

                                        cicloDeVida,

                                        cameraSelector,

                                        preview,

                                        imageAnalysis
                                    )

                                } catch (e: Exception) {

                                    Log.e(
                                        "CAMARA",
                                        "Error iniciando cámara",
                                        e
                                    )
                                }

                            },

                            ContextCompat.getMainExecutor(ctx)
                        )

                        previewView
                    },

                    modifier =
                        Modifier.fillMaxSize()
                )

                // =================================================
                // INTERFAZ SOBRE LA CÁMARA
                // =================================================

                Column(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Escanea el QR de tu juego",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // ---------------------------------------------
                    // MARCO DEL ESCÁNER
                    // ---------------------------------------------
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .border(
                                BorderStroke(width = 4.dp, color = MaterialTheme.colorScheme.primary),
                                RoundedCornerShape(24.dp)
                            )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Coloca el código dentro del cuadro",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

            } else {

                // =================================================
                // PERMISO DENEGADO
                // =================================================

                Column(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Warning,

                        contentDescription = null,

                        modifier =
                            Modifier.size(90.dp),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .error
                    )

                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )

                    Text(

                        text =
                            "Se necesita acceso a la cámara para escanear los códigos QR de tus juegos.",

                        textAlign =
                            TextAlign.Center,

                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge
                    )

                    Spacer(
                        modifier =
                            Modifier.height(32.dp)
                    )

                    Button(

                        onClick = {
                            val activity = contexto as? Activity
                            val mostrarExplicacion = activity?.let {
                                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                            } ?: false

                            if (!permisoConcedido && !mostrarExplicacion) {
                                // Esto suele indicar que el usuario marcó "No volver a preguntar"
                                // o que es la primera vez y el sistema decidirá si muestra el diálogo.
                                // Para asegurar, lanzamos el pedido primero.
                                lanzadorPermiso.launch(Manifest.permission.CAMERA)
                                
                                // Si sigue sin permiso y ya se pidió antes (mostrarExplicacion es false),
                                // redirigimos a Ajustes.
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", contexto.packageName, null)
                                }
                                contexto.startActivity(intent)
                            } else {
                                lanzadorPermiso.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CameraAlt,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text(
                            "Otorgar permiso"
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// PROCESAR IMAGEN CON ML KIT
// =============================================================

@OptIn(ExperimentalGetImage::class)
private fun procesarImagenQR(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onQrLeido: (String) -> Unit
) {

    val mediaImage =
        imageProxy.image

    if (mediaImage == null) {

        imageProxy.close()

        return
    }

    val inputImage =
        InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

    scanner.process(inputImage)

        .addOnSuccessListener { barcodes ->

            for (barcode in barcodes) {

                val valor =
                    barcode.rawValue

                if (!valor.isNullOrBlank()) {

                    onQrLeido(valor)

                    break
                }
            }
        }

        .addOnFailureListener { error ->

            Log.e(
                "QR_ERROR",
                "Error leyendo QR",
                error
            )
        }

        .addOnCompleteListener {

            // MUY IMPORTANTE:
            // liberar el frame de CameraX
            imageProxy.close()
        }
}


// =============================================================
// CONVERTIR JSON DEL QR -> JUEGO
// =============================================================

private fun procesarQR(
    valorLeido: String,
    viewModel: JuegosViewModel,
    contexto: android.content.Context,
    navController: NavController,
    alFallar: () -> Unit
) {

    try {

        Log.d(
            "QR_LEIDO",
            valorLeido
        )

        val gson = Gson()

        val juego =
            gson.fromJson(
                valorLeido,
                Juego::class.java
            )

        // =====================================================
        // VALIDACIÓN
        // =====================================================

        if (juego.id <= 0) {
            throw IllegalArgumentException(
                "ID de juego inválido"
            )
        }

        if (juego.titulo.isBlank()) {
            throw IllegalArgumentException(
                "Título vacío"
            )
        }

        if (juego.desarrollador.isBlank()) {
            throw IllegalArgumentException(
                "Desarrollador vacío"
            )
        }

        // =====================================================
        // VALIDACIÓN DE EXISTENCIA
        // =====================================================

        val juegosActuales = viewModel.todosLosJuegosLocales.value
        val yaExiste = juegosActuales.any { it.id == juego.id }

        if (yaExiste) {
            Toast.makeText(
                contexto,
                "El juego '${juego.titulo}' ya existe en tu colección.",
                Toast.LENGTH_SHORT
            ).show()
            
            // Esperamos 2 segundos antes de permitir otro escaneo para evitar spam
            Executors.newSingleThreadScheduledExecutor().schedule({
                alFallar()
            }, 2000, TimeUnit.MILLISECONDS)
            return
        }

        // =====================================================
        // GUARDAR EN ROOM
        // =====================================================

        viewModel.guardarJuego(juego)

        Toast.makeText(
            contexto,
            "¡${juego.titulo} añadido a tu colección!",
            Toast.LENGTH_LONG
        ).show()

        // Volvemos a Mi Colección
        navController.popBackStack()

    } catch (e: JsonSyntaxException) {

        Log.e(
            "QR_ERROR",
            "El contenido del QR no tiene un JSON válido",
            e
        )

        Toast.makeText(
            contexto,
            "El QR no contiene un juego válido",
            Toast.LENGTH_SHORT
        ).show()
        
        // Esperamos un poco antes de permitir otro escaneo para no saturar con Toasts
        Executors.newSingleThreadScheduledExecutor().schedule({
            alFallar()
        }, 2000, java.util.concurrent.TimeUnit.MILLISECONDS)

    } catch (e: Exception) {

        Log.e(
            "QR_ERROR",
            "Error procesando juego",
            e
        )

        Toast.makeText(
            contexto,
            "QR inválido. Revisa los datos del juego.",
            Toast.LENGTH_SHORT
        ).show()

        // Esperamos un poco antes de permitir otro escaneo
        Executors.newSingleThreadScheduledExecutor().schedule({
            alFallar()
        }, 2000, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
}