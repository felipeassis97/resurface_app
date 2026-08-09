package com.resurface.resurface.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.data.outcome.AlertResponse
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.di.IoDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Recebe o toque nos botões do aviso e grava a resposta (F7). */
@AndroidEntryPoint
class OutcomeReceiver : BroadcastReceiver() {

    @Inject lateinit var outcomes: OutcomeRepository
    @Inject lateinit var notifier: Notifier
    @Inject lateinit var time: TimeProvider
    @Inject @IoDispatcher lateinit var io: CoroutineDispatcher

    /** Mapeia a ação → resposta e persiste, fechando o aviso. */
    override fun onReceive(context: Context, intent: Intent) {
        val response = when (intent.action) {
            ACTION_ERA_HORA -> AlertResponse.ERA_HORA
            ACTION_AGORA_NAO -> AlertResponse.AGORA_NAO
            else -> return
        }
        val id = intent.getLongExtra(EXTRA_ALERT_ID, -1L)
        if (id < 0) return
        val pending = goAsync()
        CoroutineScope(io).launch {
            try {
                outcomes.recordResponse(id, response, time.now())
                notifier.cancelAlert()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_ERA_HORA = "com.resurface.resurface.ERA_HORA"
        const val ACTION_AGORA_NAO = "com.resurface.resurface.AGORA_NAO"
        const val EXTRA_ALERT_ID = "alertId"
    }
}
