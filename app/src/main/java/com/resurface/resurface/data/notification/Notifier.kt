package com.resurface.resurface.data.notification

import android.app.Notification

/** Entrega das notificações (G5): a fixa do FGS e o aviso heads-up com botões. */
interface Notifier {

    /** Cria os canais (idempotente). Chamar antes de postar. */
    fun ensureChannels()

    /** Notificação fixa de baixa importância pro FGS (contador vivo, D21). */
    fun ongoing(text: String): Notification

    /** Posta o aviso heads-up (canal HIGH) com os dois botões, ligado ao [alertId] (F7/G5). */
    fun postAlert(appLabel: String, minutes: Int, alertId: Long)

    /** Fecha o aviso (após resposta). */
    fun cancelAlert()
}
