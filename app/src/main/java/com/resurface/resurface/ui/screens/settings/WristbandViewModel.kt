package com.resurface.resurface.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.ble.AlertHaptics
import com.resurface.resurface.ble.ConnectionFailure
import com.resurface.resurface.ble.DiscoveredWristband
import com.resurface.resurface.ble.WristbandConnectionState
import com.resurface.resurface.ble.WristbandLink
import com.resurface.resurface.data.wristband.WristbandPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Um device na lista de scan, com força de sinal já derivada. */
data class DeviceRow(val address: String, val name: String, val bars: Int)

/** Estado da tela de pulseira, derivado do link + scan. */
sealed interface WristbandUiState {
    data object Rest : WristbandUiState
    data class Scanning(val devices: List<DeviceRow>, val searching: Boolean) : WristbandUiState
    data object Empty : WristbandUiState
    data class Connecting(val name: String) : WristbandUiState
    data class Connected(val name: String, val intensity: Int?) : WristbandUiState
    data class Failed(val message: String) : WristbandUiState
}

@HiltViewModel
class WristbandViewModel @Inject constructor(
    private val link: WristbandLink,
    private val prefs: WristbandPreferences,
    private val haptics: AlertHaptics,
) : ViewModel() {

    /** Verdadeiro depois que o usuário pediu um scan (pra distinguir Rest de Empty). */
    private val scanRequested = MutableStateFlow(false)

    val uiState: StateFlow<WristbandUiState> =
        combine(link.state, link.scanResults, prefs.intensity, scanRequested) { state, results, intensity, requested ->
            derive(state, results, intensity, requested)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WristbandUiState.Rest)

    fun onScan() {
        scanRequested.value = true
        link.startScan()
    }

    fun onConnect(address: String) = link.connect(address)

    fun onDisconnect() {
        scanRequested.value = false
        link.disconnect()
    }

    fun onForget() {
        scanRequested.value = false
        link.forget()
    }

    fun onTestPulse() = haptics.pulse()

    fun onSetIntensity(value: Int?) {
        viewModelScope.launch { prefs.setIntensity(value) }
    }

    private fun derive(
        state: WristbandConnectionState,
        results: List<DiscoveredWristband>,
        intensity: Int?,
        requested: Boolean,
    ): WristbandUiState = when (state) {
        is WristbandConnectionState.Connected -> WristbandUiState.Connected(
            name = state.name?.takeIf { it.isNotBlank() } ?: "Wristband",
            intensity = intensity,
        )
        is WristbandConnectionState.Connecting -> WristbandUiState.Connecting("Wristband (${state.address.takeLast(5)})")
        is WristbandConnectionState.Failed -> WristbandUiState.Failed(failureMessage(state.reason))
        WristbandConnectionState.Scanning -> WristbandUiState.Scanning(results.map(::toRow), searching = true)
        WristbandConnectionState.Idle, is WristbandConnectionState.Disconnected -> when {
            requested && results.isNotEmpty() -> WristbandUiState.Scanning(results.map(::toRow), searching = false)
            requested -> WristbandUiState.Empty
            else -> WristbandUiState.Rest
        }
    }

    private fun toRow(d: DiscoveredWristband) = DeviceRow(d.address, d.displayName, signalBars(d.rssi))

    private fun signalBars(rssi: Int): Int = when {
        rssi >= -60 -> 4
        rssi >= -70 -> 3
        rssi >= -80 -> 2
        else -> 1
    }

    private fun failureMessage(reason: ConnectionFailure): String = when (reason) {
        ConnectionFailure.PERMISSION_DENIED -> "Bluetooth permission is off. Allow it and try again."
        ConnectionFailure.BLUETOOTH_UNAVAILABLE -> "Bluetooth is off. Turn it on and try again."
        ConnectionFailure.COMMAND_CHARACTERISTIC_MISSING -> "This device is not a compatible wristband."
        ConnectionFailure.GATT_ERROR -> "Connection failed. Try again."
        ConnectionFailure.TIMEOUT -> "It took too long. Try again."
    }
}
