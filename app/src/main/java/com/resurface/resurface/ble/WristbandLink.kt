package com.resurface.resurface.ble

import kotlinx.coroutines.flow.StateFlow

/**
 * Superfície do link BLE que a UI/serviço consomem (seam pra testar sem o stack BLE real).
 * Implementado por [WristbandRepository].
 */
interface WristbandLink {
    /** Estado atual do link, observável. */
    val state: StateFlow<WristbandConnectionState>

    /** Matches do último scan, mantidos após ele terminar. */
    val scanResults: StateFlow<List<DiscoveredWristband>>

    /** Inicia um scan limitado pela pulseira. */
    fun startScan()

    /** Para o scan em curso. */
    fun stopScan()

    /** Conecta a um device escolhido. */
    fun connect(address: String)

    /** Fecha o link atual (não mexe no device lembrado). */
    fun disconnect()

    /** Desconecta e esquece o device lembrado (não reconecta passivamente depois). */
    fun forget()

    /** Reconecta ao device lembrado, passivo; no-op se nada lembrado. */
    fun reconnectRemembered()
}
