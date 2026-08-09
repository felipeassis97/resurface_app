package com.resurface.resurface.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.resurface.resurface.data.behavior.BehaviorRepository
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.SwipeDetector
import com.resurface.resurface.domain.model.ScrollEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fonte de COMPORTAMENTO (F5), separada do tempo (D13). Escuta scroll nos 2 alvos, agrupa os
 * deslizes ao vivo (flush no gap > 0,5 s ou na troca de janela) e persiste os vídeos detectados.
 * Não conhece o contador/alarme — se quebrar por update do app, o tempo segue intacto.
 */
@AndroidEntryPoint
class ResurfaceAccessibilityService : AccessibilityService() {

    @Inject lateinit var behavior: BehaviorRepository
    @Inject lateinit var time: TimeProvider
    @Inject @IoDispatcher lateinit var io: CoroutineDispatcher

    private val detector = SwipeDetector()
    private val scope by lazy { CoroutineScope(io + SupervisorJob()) }

    private var group = mutableListOf<ScrollEvent>()
    private var lastTs = 0L

    /** Roteia o evento: scroll acumula; troca de janela fecha o grupo corrente. */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> onScroll(event)
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> flush()
        }
    }

    /** Adiciona o scroll ao grupo; fecha o anterior se o gap passou de 0,5 s. */
    private fun onScroll(event: AccessibilityEvent) {
        val e = ScrollEvent(
            className = event.className?.toString() ?: "",
            dy = event.scrollDeltaY,
            dx = event.scrollDeltaX,
            timestamp = event.eventTime,
            pkg = event.packageName?.toString() ?: "",
        )
        if (group.isNotEmpty() && e.timestamp - lastTs > GAP_MS) flush()
        group.add(e)
        lastTs = e.timestamp
    }

    /** Classifica o grupo fechado e persiste se for um vídeo. */
    private fun flush() {
        if (group.isEmpty()) return
        val closed = group
        group = mutableListOf()
        // Grouping usa o eventTime (uptime, monotônico); a persistência precisa de wall-clock (epoch)
        // pra o dashboard filtrar por semana/dia — por isso carimba com time.now() no flush.
        detector.classifyGroup(closed)?.let { swipe ->
            val stamped = swipe.copy(timestamp = time.now())
            scope.launch { behavior.record(stamped) }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val GAP_MS = 500L
    }
}
