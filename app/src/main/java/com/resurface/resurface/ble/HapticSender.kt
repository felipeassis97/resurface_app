package com.resurface.resurface.ble

/**
 * Seam estreito de envio (só o que o [AlertHaptics] precisa). Implementado por
 * [WristbandRepository]; permite testar o disparo do pulso sem tocar no stack BLE.
 */
interface HapticSender {
    /** Escreve um comando na pulseira; NotConnected se não há link. */
    suspend fun send(command: HapticCommand): SendResult
}
