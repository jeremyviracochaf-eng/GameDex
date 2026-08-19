package com.example.gamedex

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Instancia única de DataStore
val Context.dataStore by preferencesDataStore(name = "ajustes_app_juegos")

class AjustesPreferencias(private val context: Context) {

    companion object {
        // Preferencia del modo oscuro
        val MODO_OSCURO = booleanPreferencesKey("modo_oscuro_activado")

        // Preferencias del perfil gamer
        val GAMERTAG = stringPreferencesKey("gamertag_usuario")
        val FRASE = stringPreferencesKey("frase_usuario")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
    }

    // ---------------------------------------------------------
    // MODO OSCURO
    // ---------------------------------------------------------

    suspend fun guardarModoOscuro(activado: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[MODO_OSCURO] = activado
        }
    }

    val modoOscuroFlow: Flow<Boolean> =
        context.dataStore.data.map { preferencias ->
            preferencias[MODO_OSCURO] ?: false
        }

    // ---------------------------------------------------------
    // PERFIL GAMER
    // ---------------------------------------------------------

    suspend fun guardarPerfil(
        gamertag: String,
        frase: String,
        uriString: String
    ) {
        context.dataStore.edit { preferencias ->
            preferencias[GAMERTAG] = gamertag
            preferencias[FRASE] = frase
            preferencias[AVATAR_URI] = uriString
        }
    }

    val gamertagFlow: Flow<String> =
        context.dataStore.data.map { preferencias ->
            preferencias[GAMERTAG] ?: "Jugador_1"
        }

    val fraseFlow: Flow<String> =
        context.dataStore.data.map { preferencias ->
            preferencias[FRASE] ?: "Coleccionando aventuras 🎮"
        }

    val avatarUriFlow: Flow<String> =
        context.dataStore.data.map { preferencias ->
            preferencias[AVATAR_URI] ?: ""
        }
}