package com.example.gamedex

import com.google.gson.annotations.SerializedName

// Este es el modelo exacto que nos envía FreeToGame
data class JuegoRed(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val titulo: String,
    @SerializedName("short_description") val descripcionCorta: String,
    @SerializedName("thumbnail") val miniaturaUrl: String,
    @SerializedName("developer") val desarrollador: String,
    @SerializedName("release_date") val fechaLanzamiento: String
) {
    // Convertimos lo que llega de internet a nuestro formato local (Room)
    fun aJuegoLocal(): Juego {
        return Juego(
            id = this.id,
            titulo = this.titulo,
            desarrollador = this.desarrollador,
            // Extraemos solo el año (ej. "2020-09-28" -> 2020), o 0 si falla
            anioLanzamiento = this.fechaLanzamiento.take(4).toIntOrNull() ?: 0,
            sinopsis = this.descripcionCorta,
            imagenUrl = this.miniaturaUrl,
            metacritic = 0, // FreeToGame no da puntaje Metacritic, lo dejamos en 0
            estadoJuego = "Pendiente" // Estado por defecto para tu Backlog
        )
    }
}