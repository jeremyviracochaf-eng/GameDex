package com.example.gamedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamedex.ui.theme.GameDexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val contexto = LocalContext.current

            // Inicialización de componentes (idealmente en Application o DI)
            val baseDatos = remember { BaseDatosJuegos.obtenerInstancia(contexto) }
            val dao = remember { baseDatos.juegoDao() }
            val ajustesPreferencias = remember { AjustesPreferencias(contexto) }
            val repositorio = remember { RepositorioJuegos(dao) }

            // Instanciamos el ViewModel
            val viewModel: JuegosViewModel = viewModel(
                factory = JuegosViewModelFactory(repositorio, ajustesPreferencias),
            )

            // Observamos el estado del modo oscuro para aplicarlo al tema
            val modoOscuroActivado by viewModel.esModoOscuro.collectAsState()

            GameDexTheme(darkTheme = modoOscuroActivado) {
                // Usamos Surface en lugar de Scaffold aquí para que el fondo base se adapte al tema (Claro/Oscuro)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Lanzamos nuestro motor de navegación pasándole el ViewModel
                    AppNavegacion(viewModel = viewModel)
                }
            }
        }
    }
}
