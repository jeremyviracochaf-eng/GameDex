package com.example.gamedex

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavegacion(viewModel: JuegosViewModel) { // <-- Añadimos el parámetro aquí
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "explorar_api") {
        composable("explorar_api") {
            PantallaExplorar(navController = navController, viewModel = viewModel)
        }
        composable("mi_coleccion") {
            PantallaColeccion(navController = navController, viewModel = viewModel)
        }
        composable("detalle/{juegoId}") { backStackEntry ->
            val juegoIdStr = backStackEntry.arguments?.getString("juegoId")
            val juegoId = juegoIdStr?.toIntOrNull() ?: 0

            PantallaDetalleJuego(navController = navController, viewModel = viewModel, juegoId = juegoId)
        }
        composable("escaner_qr") {
            PantallaEscanerQR(navController = navController, viewModel = viewModel)
        }
        composable("ajustes") {
            PantallaAjustes(navController = navController, viewModel = viewModel)
        }
    }
}