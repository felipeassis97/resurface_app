package com.resurface.resurface.data.notification

/** Ids dos canais de notificação, compartilhados entre o Notifier e a leitura de avisos próprios. */
object NotificationChannels {
    /** Canal do aviso (IMPORTANCE_HIGH, heads-up). Só ESTE conta como "aviso disparado" (D24). */
    const val ALERT = "resurface_alert"

    /** Canal da notificação fixa do FGS (IMPORTANCE_LOW). NÃO conta como aviso. */
    const val ONGOING = "resurface_ongoing"
}
