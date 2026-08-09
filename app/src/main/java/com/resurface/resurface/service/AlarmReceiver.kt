package com.resurface.resurface.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.resurface.resurface.di.IoDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Dispara quando o alarme exato bate: delega o "acordar-pra-conferir" ao [AlertEvaluator] (D22). */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var evaluator: AlertEvaluator
    @Inject @IoDispatcher lateinit var io: CoroutineDispatcher

    /** Relê, recomputa e posta (ou reagenda) fora da main, mantendo o broadcast vivo. */
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(io).launch {
            try {
                evaluator.onAlarmFired()
            } finally {
                pending.finish()
            }
        }
    }
}
