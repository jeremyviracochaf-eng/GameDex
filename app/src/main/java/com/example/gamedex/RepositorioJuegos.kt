package com.example.gamedex

import kotlinx.coroutines.flow.Flow

class RepositorioJuegos(private val juegoDao: JuegoDao) {

    // 1. LECTURA: Exponemos los flujos de datos directamente desde el DAO
    val todosLosJuegosLocales: Flow<List<Juego>> = juegoDao.obtenerTodosLosJuegos()

    val totalJuegos: Flow<Int> =
        juegoDao.contarJuegosTotales()

    fun cantidadPorEstado(estado: String): Flow<Int> {
        return juegoDao.contarJuegosPorEstado(estado)
    }

    // 2. ESCRITURA: Funciones suspendidas para modificar datos
    suspend fun guardarJuegoLocal(juego: Juego) {
        juegoDao.insertarJuego(juego)
    }

    suspend fun obtenerJuegoEspecifico(id: Int): Juego? {
        return juegoDao.obtenerJuegoPorId(id)
    }

    suspend fun obtenerJuegosDeInternet(): List<JuegoRed> {
        return try {
            // Llamada directa sin parámetros
            ClienteRetrofit.api.obtenerJuegosGratis()
        } catch (e: Exception) {
            android.util.Log.e("API_ERROR", "Error descargando juegos: ${e.message}")
            emptyList()
        }
    }
}
