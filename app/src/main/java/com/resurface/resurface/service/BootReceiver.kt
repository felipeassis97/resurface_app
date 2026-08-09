package com.resurface.resurface.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Religa o [MonitorService] após reboot/atualização (D24/G3). Funciona sob as 2 condições
 * medidas: primeiro desbloqueio (FBE) + isenção de bateria (allowlist).
 */
class BootReceiver : BroadcastReceiver() {

    /** Sobe o FGS de novo — contexto permitido pra iniciar FGS no boot. */
    override fun onReceive(context: Context, intent: Intent) {
        context.startForegroundService(Intent(context, MonitorService::class.java))
    }
}
