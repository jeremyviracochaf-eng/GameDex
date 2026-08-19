package com.example.gamedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

class JuegosViewModel(
    private val repositorio: RepositorioJuegos,
    private val ajustesPreferencias: AjustesPreferencias,
) : ViewModel() {

    // 1. OBSERVADORES DE ROOM (Base de datos local)
    // Convertimos el Flow del Repositorio a un StateFlow para que la UI (Compose) lo lea fácilmente
    val todosLosJuegosLocales: StateFlow<List<Juego>> = repositorio.todosLosJuegosLocales
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun guardarJuego(juego: Juego) {
        viewModelScope.launch {
            repositorio.guardarJuegoLocal(juego)
        }
    }

    // 2. OBSERVADORES DE DATASTORE (Preferencias)
    val esModoOscuro: StateFlow<Boolean> = ajustesPreferencias.modoOscuroFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // ---------------------------------------------------------
    // ESTADÍSTICAS DE ROOM
    // ---------------------------------------------------------

    val totalJuegos: StateFlow<Int> =
        repositorio.totalJuegos
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    val juegosTerminados: StateFlow<Int> =
        repositorio.cantidadPorEstado("Terminado")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    val juegosJugando: StateFlow<Int> =
        repositorio.cantidadPorEstado("Jugando")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    val juegosPendientes: StateFlow<Int> =
        repositorio.cantidadPorEstado("Pendiente")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )


    // ---------------------------------------------------------
    // PERFIL GAMER - DATASTORE
    // ---------------------------------------------------------

    val gamertag: StateFlow<String> =
        ajustesPreferencias.gamertagFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "Jugador_1"
            )

    val frase: StateFlow<String> =
        ajustesPreferencias.fraseFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "Coleccionando aventuras 🎮"
            )

    val avatarUri: StateFlow<String> =
        ajustesPreferencias.avatarUriFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ""
            )

    fun guardarPerfil(
        gamertag: String,
        frase: String,
        avatarUri: String
    ) {
        viewModelScope.launch {
            ajustesPreferencias.guardarPerfil(
                gamertag = gamertag,
                frase = frase,
                uriString = avatarUri
            )
        }
    }

    fun cambiarModoOscuro(activado: Boolean) {
        viewModelScope.launch {
            ajustesPreferencias.guardarModoOscuro(activado)
        }
    }

    private val _juegosInternet = MutableStateFlow<List<JuegoRed>>(emptyList())
    val juegosInternet: StateFlow<List<JuegoRed>> = _juegosInternet

    private val _estaCargandoApi = MutableStateFlow(value = false)
    val estaCargandoApi: StateFlow<Boolean> = _estaCargandoApi


    fun cargarJuegosInternet() {
        viewModelScope.launch {
            _estaCargandoApi.value = true

            // Ya no hace falta apiKey
            val listaRemota = repositorio.obtenerJuegosDeInternet()
            if (listaRemota.isNotEmpty()) {
                _juegosInternet.value = listaRemota
            }

            _estaCargandoApi.value = false
        }
    }

    suspend fun obtenerJuegoPorId(id: Int): Juego? {
        return repositorio.obtenerJuegoEspecifico(id)
    }

    suspend fun obtenerJuegoParaDetalle(id: Int): Juego? {
        // Primero buscamos en Room
        val juegoLocal = repositorio.obtenerJuegoEspecifico(id)

        if (juegoLocal != null) {
            return juegoLocal
        }

        // Si no está guardado, lo buscamos en la API
        val juegoInternet = juegosInternet.value.find {
            it.id == id
        }

        // Lo convertimos temporalmente al modelo de Room
        return juegoInternet?.aJuegoLocal()
    }

    fun actualizarEstadoJuego(juego: Juego, nuevoEstado: String) {
        viewModelScope.launch {
            // Como usamos REPLACE en el DAO, al guardar el mismo ID se actualizan los datos
            val juegoActualizado = juego.copy(estadoJuego = nuevoEstado)
            repositorio.guardarJuegoLocal(juegoActualizado)
        }
    }
}

// ---------------------------------------------------------
// FACTORY: Es necesario para poder inyectar el Repositorio
// y el DataStore al ViewModel cuando la app inicie.
// ---------------------------------------------------------
class JuegosViewModelFactory(
    private val repositorio: RepositorioJuegos,
    private val ajustesPreferencias: AjustesPreferencias
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JuegosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JuegosViewModel(repositorio, ajustesPreferencias) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}