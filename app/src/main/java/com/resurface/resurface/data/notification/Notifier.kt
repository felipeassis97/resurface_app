package com.resurface.resurface.data.notification

import android.app.Notification

/** Entrega das notificações (G5): a fixa do FGS e o aviso heads-up com botões. */
interface Notifier {

    /** Cria os canais (idempotente). Chamar antes de postar. */
    fun ensureChannels()

    /** Notificação fixa de baixa importância pro FGS (contador vivo, D21). */
    fun ongoing(text: String): Notification

    /** Posta o aviso heads-up (canal HIGH) com o texto composto + os dois botões, ligado ao [alertId]. */
    fun postAlert(title: String, body: String, alertId: Long)

    /** Fecha o aviso (após resposta). */
    fun cancelAlert()
}
