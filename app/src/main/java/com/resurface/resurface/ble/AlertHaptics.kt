package com.resurface.resurface.ble

import com.resurface.resurface.data.wristband.WristbandPreferences
import com.resurface.resurface.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seam entre a notificação e a pulseira (D-2). O `NotifierImpl` chama [pulse] ao postar um
 * aviso, sem depender de BLE direto. Fire-and-forget: nunca atrasa nem quebra a notificação.
 */
interface AlertHaptics {
    /** Dispara um pulso Gentle na intensidade configurada; no-op se não há pulseira. */
    fun pulse()
}

/**
 * Impl real: lê a intensidade gravada e manda um `Gentle` pra pulseira num escopo próprio.
 * `send()` já é no-op quando não conectado (canSend=false), então "sem pulseira" não faz nada.
 */
@Singleton
class WristbandAlertHaptics @Inject constructor(
    private val sender: HapticSender,
    private val preferences: WristbandPreferences,
    @IoDispatcher io: CoroutineDispatcher,
) : AlertHaptics {

    private val scope = CoroutineScope(io + SupervisorJob())

    /** Envia o pulso fora da thread do chamador; falha/ausência de link é silenciosa. */
    override fun pulse() {
        scope.launch { pulseOnce() }
    }

    /** Núcleo (testável): lê a intensidade e envia um Gentle. */
    internal suspend fun pulseOnce() {
        val intensity = preferences.intensity.first()
        sender.send(HapticCommand(HapticEffect.Gentle, intensity))
    }
}
