package com.example.gamedex

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    // Definición de las 5 pantallas obligatorias del proyecto
    NavHost(navController = navController, startDestination = "explorar_api") {

        // 1. Pantalla para consumir Retrofit (Inicio)
        composable("explorar_api") {
            PantallaExplorar(navController = navController)
        }

        // 2. Pantalla para leer desde Room (Tu Backlog local)
        composable("mi_coleccion") {
            PantallaColeccion(navController = navController)
        }

        // 3. Pantalla de detalles del juego seleccionado
        composable("detalle/{juegoId}") { backStackEntry ->
            val juegoIdStr = backStackEntry.arguments?.getString("juegoId")
            val juegoId = juegoIdStr?.toIntOrNull() ?: 0

            PantallaDetalleJuego(navController = navController, juegoId = juegoId)
        }

        // 4. Pantalla de Hardware (Escáner QR con la cámara)
        composable("escaner_qr") {
            PantallaEscanerQR(navController = navController)
        }

        // 5. Pantalla de Ajustes (DataStore)
        composable("ajustes") {
            PantallaAjustes(navController = navController)
        }
    }
}