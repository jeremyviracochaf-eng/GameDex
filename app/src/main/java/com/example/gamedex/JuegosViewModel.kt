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
    private val ajustesPreferencias: AjustesPreferencias
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

    fun cambiarModoOscuro(activado: Boolean) {
        viewModelScope.launch {
            ajustesPreferencias.guardarModoOscuro(activado)
        }
    }

    private val _juegosInternet = MutableStateFlow<List<JuegoRed>>(emptyList())
    val juegosInternet: StateFlow<List<JuegoRed>> = _juegosInternet

    private val _estaCargandoApi = MutableStateFlow(false)
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