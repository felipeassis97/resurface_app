package com.resurface.resurface.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.resurface.resurface.service.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Impl real: `setExactAndAllowWhileIdle` com `ELAPSED_REALTIME_WAKEUP` (D22, validado 12 ms em
 * Doze — GAPS G1). PendingIntent imutável pro AlarmReceiver. Chamadas espelham o probe.
 */
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
        context,
        REQ_CODE,
        Intent(context, AlarmReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** Agenda o alarme exato daqui a [delayMs], se a permissão de alarme exato permitir. */
    override fun scheduleInMs(delayMs: Long) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
        if (!canExact) return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + delayMs.coerceAtLeast(0L),
            pendingIntent,
        )
    }

    /** Cancela o alarme pendente. */
    override fun cancel() {
        alarmManager.cancel(pendingIntent)
    }

    private companion object {
        const val REQ_CODE = 1001
    }
}
