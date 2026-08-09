package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.DetectedSwipe
import com.resurface.resurface.domain.model.ScrollEvent
import com.resurface.resurface.domain.model.Surface

/**
 * Aplica a regra de contagem de vídeo validada no aparelho (REELS.md/TIKTOK.md): vídeo = grupo
 * de eventos `ViewPager` com `dy≠0`, `dx=0`, separado por gap > 0,5 s. Pura e testável (G1).
 */
class SwipeDetector {

    /** Separa a lista em grupos por gap > 0,5 s e classifica cada um; devolve os vídeos. */
    fun detect(events: List<ScrollEvent>): List<DetectedSwipe> {
        if (events.isEmpty()) return emptyList()
        val sorted = events.sortedBy { it.timestamp }
        val out = ArrayList<DetectedSwipe>()
        var group = mutableListOf(sorted.first())
        for (e in sorted.drop(1)) {
            if (e.timestamp - group.last().timestamp > GAP_MS) {
                classifyGroup(group)?.let(out::add)
                group = mutableListOf(e)
            } else {
                group.add(e)
            }
        }
        classifyGroup(group)?.let(out::add)
        return out
    }

    /** Classifica um grupo já fechado: vídeo (Reels) só se ViewPager, `dx=0`, algum `dy≠0`. */
    fun classifyGroup(group: List<ScrollEvent>): DetectedSwipe? {
        if (group.isEmpty()) return null
        if (group.any { it.dx != 0 }) return null                      // navegação lateral (A12)
        if (group.none { it.dy != 0 }) return null                     // sem movimento vertical
        if (group.any { !it.className.contains("ViewPager") }) return null  // só ViewPager = vídeo
        val hesitated = group.any { it.dy < 0 }                        // deslize voltou (H7)
        return DetectedSwipe(group.first().timestamp, group.first().pkg, Surface.REELS, hesitated)
    }

    private companion object {
        const val GAP_MS = 500L
    }
}
