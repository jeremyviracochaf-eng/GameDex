package com.example.gamedex

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Instancia única de DataStore
val Context.dataStore by preferencesDataStore(name = "ajustes_app_juegos")

class AjustesPreferencias(private val context: Context) {

    companion object {
        val MODO_OSCURO = booleanPreferencesKey("modo_oscuro_activado")
    }

    // Función para guardar el estado del switch
    suspend fun guardarModoOscuro(activado: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[MODO_OSCURO] = activado
        }
    }

    // Flujo para leer el estado en tiempo real (por defecto será false/claro)
    val modoOscuroFlow: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        preferencias[MODO_OSCURO] ?: false
    }
}

