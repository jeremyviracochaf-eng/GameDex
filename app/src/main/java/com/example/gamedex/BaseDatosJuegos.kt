package com.example.gamedex

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Juego::class], version = 1, exportSchema = false)
abstract class BaseDatosJuegos : RoomDatabase() {

    // Función abstracta para enlazar el DAO
    abstract fun juegoDao(): JuegoDao

    companion object {
        @Volatile
        private var INSTANCE: BaseDatosJuegos? = null

        fun obtenerInstancia(context: Context): BaseDatosJuegos {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosJuegos::class.java,
                    "base_datos_videojuegos"
                ).build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}