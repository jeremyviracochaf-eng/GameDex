package com.example.gamedex

import kotlinx.coroutines.flow.Flow

class RepositorioJuegos(private val juegoDao: JuegoDao) {

    // 1. LECTURA: Exponemos los flujos de datos directamente desde el DAO
    val todosLosJuegosLocales: Flow<List<Juego>> = juegoDao.obtenerTodosLosJuegos()

    fun juegosPorEstado(estado: String): Flow<List<Juego>> {
        return juegoDao.obtenerJuegosPorEstado(estado)
    }

    // 2. ESCRITURA: Funciones suspendidas para modificar datos
    suspend fun guardarJuegoLocal(juego: Juego) {
        juegoDao.insertarJuego(juego)
    }

    suspend fun obtenerJuegoEspecifico(id: Int): Juego? {
        return juegoDao.obtenerJuegoPorId(id)
    }

    // Nota: Más adelante, cuando configuremos Retrofit (la API),
    // inyectaremos también la API aquí y agregaremos las funciones
    // para descargar datos de internet.
}
