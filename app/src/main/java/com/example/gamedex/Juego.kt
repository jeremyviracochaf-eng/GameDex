package com.example.gamedex

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "juegos_guardados")
data class Juego(
    @PrimaryKey val id: Int, // Usaremos el ID real de la API
    val titulo: String,
    val desarrollador: String,
    val anioLanzamiento: Int,
    val sinopsis: String,
    val imagenUrl: String,
    val metacritic: Int, // Puntaje en lugar de calificación genérica
    val estadoJuego: String = "Pendiente" // Puede ser: "Pendiente", "Jugando", "Terminado"
)
