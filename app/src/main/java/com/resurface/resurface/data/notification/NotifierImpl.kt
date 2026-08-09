package com.resurface.resurface.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.resurface.resurface.ble.AlertHaptics
import com.resurface.resurface.service.OutcomeReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Impl real. Canal HIGH faz o heads-up sobre tela cheia (validado GAPS G5); canal LOW é a
 * notificação fixa do FGS. Botões via PendingIntent imutável, requestCode único por ação.
 * Todo aviso postado também pulsa a pulseira via [haptics] (fire-and-forget, D-2).
 */
class NotifierImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val haptics: AlertHaptics,
) : Notifier {

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /** Cria os dois canais; a importância trava após criar (por isso ids fixos). */
    override fun ensureChannels() {
        manager.createNotificationChannel(
            NotificationChannel(CH_ONGOING, "Contador", NotificationManager.IMPORTANCE_LOW)
        )
        manager.createNotificationChannel(
            NotificationChannel(CH_ALERT, "Aviso", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    /** Monta a notificação fixa do FGS (baixa, sem som, recolhida). */
    override fun ongoing(text: String): Notification {
        ensureChannels()
        return NotificationCompat.Builder(context, CH_ONGOING)
            .setContentTitle("Resurface")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /** Posta o aviso com o texto composto (perfil+momento) + os dois botões ligados ao [alertId]. */
    override fun postAlert(title: String, body: String, alertId: Long) {
        ensureChannels()
        val notification = NotificationCompat.Builder(context, CH_ALERT)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "era hora", action(OutcomeReceiver.ACTION_ERA_HORA, alertId))
            .addAction(0, "agora não", action(OutcomeReceiver.ACTION_AGORA_NAO, alertId))
            .build()
        manager.notify(NOTIF_ALERT, notification)
        // Canal sem tela: pulsa a pulseira junto (no-op se não conectada). Todo aviso, incl. teste.
        haptics.pulse()
    }

    /** Fecha o aviso. */
    override fun cancelAlert() = manager.cancel(NOTIF_ALERT)

    /** PendingIntent imutável pro OutcomeReceiver, requestCode único por ação. */
    private fun action(actionName: String, alertId: Long): PendingIntent {
        val intent = Intent(context, OutcomeReceiver::class.java)
            .setAction(actionName)
            .putExtra(OutcomeReceiver.EXTRA_ALERT_ID, alertId)
        return PendingIntent.getBroadcast(
            context,
            actionName.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        const val CH_ONGOING = NotificationChannels.ONGOING
        const val CH_ALERT = NotificationChannels.ALERT
        const val NOTIF_ALERT = 2
    }
}
