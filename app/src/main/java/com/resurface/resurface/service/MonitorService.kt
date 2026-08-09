package com.resurface.resurface.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import com.resurface.resurface.ble.WristbandRepository
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.di.IoDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FGS `specialUse` (D20, validado G4) que mantém o contador vivo. Serviço FINO: só bombeia o
 * tick de manutenção pro [AlertEvaluator]. O disparo do aviso é do alarme exato, não deste tick.
 */
@AndroidEntryPoint
class MonitorService : Service() {

    @Inject lateinit var evaluator: AlertEvaluator
    @Inject lateinit var notifier: Notifier
    @Inject lateinit var wristband: WristbandRepository
    @Inject @IoDispatcher lateinit var io: CoroutineDispatcher

    private var scope: CoroutineScope? = null

    /** Sobe em primeiro plano e inicia o loop de tick (idempotente entre re-entregas). */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notifier.ensureChannels()
        startForeground(
            NOTIF_ONGOING,
            notifier.ongoing("acompanhando"),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
        // Auto-reconecta à pulseira lembrada (passivo, no-op se nada lembrado/sem permissão).
        wristband.reconnectRemembered()
        if (scope == null) {
            val s = CoroutineScope(io + SupervisorJob())
            scope = s
            s.launch {
                while (isActive) {
                    runCatching { evaluator.refresh() }
                    delay(TICK_MS)
                }
            }
        }
        return START_STICKY
    }

    /** Encerra o loop de tick. */
    override fun onDestroy() {
        scope?.cancel()
        scope = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private companion object {
        const val NOTIF_ONGOING = 1
        const val TICK_MS = 45_000L
    }
}
