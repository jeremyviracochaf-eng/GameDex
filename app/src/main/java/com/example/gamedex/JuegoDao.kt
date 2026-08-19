package com.example.gamedex

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JuegoDao {

    // Inserta un juego o lo actualiza si el ID ya existe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarJuego(juego: Juego)

    // Inserta una lista entera de juegos (útil cuando descargas de la API)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVariosJuegos(juegos: List<Juego>)

    // Obtiene TODOS los juegos locales y reacciona a cambios en tiempo real
    @Query("SELECT * FROM juegos_guardados")
    fun obtenerTodosLosJuegos(): Flow<List<Juego>>

    // Obtiene juegos filtrados por su estado (ej. "Jugando" o "Pendiente")
    @Query("SELECT * FROM juegos_guardados WHERE estadoJuego = :estado")
    fun obtenerJuegosPorEstado(estado: String): Flow<List<Juego>>

    // Busca un juego específico por su ID
    @Query("SELECT * FROM juegos_guardados WHERE id = :idJuego")
    suspend fun obtenerJuegoPorId(idJuego: Int): Juego?

    // Borra todo el catálogo local
    @Query("DELETE FROM juegos_guardados")
    suspend fun limpiarColeccion()

    // Cuenta absolutamente todos los juegos guardados
    @Query("SELECT COUNT(*) FROM juegos_guardados")
    fun contarJuegosTotales(): Flow<Int>

    // Cuenta los juegos dependiendo de si están "Terminado", "Jugando" o "Pendiente"
    @Query("SELECT COUNT(*) FROM juegos_guardados WHERE estadoJuego = :estado")
    fun contarJuegosPorEstado(estado: String): Flow<Int>
}