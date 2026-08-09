package com.resurface.resurface.domain.model

/**
 * Um evento de rolagem cru (TYPE_VIEW_SCROLLED) traduzido pro domínio. A tradução do
 * `AccessibilityEvent` pra cá é da camada de serviço; o detector fica puro (G1).
 */
data class ScrollEvent(
    val className: String,
    val dy: Int,
    val dx: Int,
    val timestamp: Long,
    val pkg: String,
)

/** Superfície onde o deslize aconteceu. */
enum class Surface { REELS, FEED, OTHER }

/** Um deslize detectado = um vídeo assistido, com superfície e se houve hesitação (H7). */
data class DetectedSwipe(
    val timestamp: Long,
    val pkg: String,
    val surface: Surface,
    val hesitated: Boolean,
)
