package com.example.gamedex

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiVideojuegos {
    // Al llamar a este endpoint nos devuelve directamente la lista de juegos
    @GET("games")
    suspend fun obtenerJuegosGratis(): List<JuegoRed>
}

object ClienteRetrofit {
    // URL base de FreeToGame
    private const val BASE_URL = "https://www.freetogame.com/api/"

    val api: ApiVideojuegos by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiVideojuegos::class.java)
    }
}