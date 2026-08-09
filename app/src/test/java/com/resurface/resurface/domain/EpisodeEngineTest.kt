package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.ClosedEpisode
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import com.resurface.resurface.domain.model.UsageEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val IG = "com.instagram.android"
private const val TT = "com.zhiliaoapp.musically"

/** Minutos em millis, pra timelines legíveis. */
private fun min(m: Double): Long = (m * 60_000).toLong()

class EpisodeEngineTest {

    private val engine = EpisodeEngine()

    /** Trocar de app no meio do episódio não zera o acumulado (D2). */
    @Test
    fun `trocar de app nao zera o acumulado`() {
        val (state, closed) = engine.run(
            listOf(
                UsageEvent.Enter(IG, min(0.0)),
                UsageEvent.Leave(IG, min(15.0)),
                UsageEvent.Enter(TT, min(15.0)),  // troca direta, dentro da janela
                UsageEvent.Leave(TT, min(22.0)),
            )
        )
        assertTrue("nenhum episódio fechado ainda", closed.isEmpty())
        assertEquals(EpisodePhase.PAUSADO, state.phase)
        assertEquals(min(22.0), state.bankedMs)              // 15 (ig) + 7 (tt)
        assertEquals(setOf(IG, TT), state.appsInEpisode)
    }

    /** Voltar em menos de 5 min retoma de onde parou (D3). */
    @Test
    fun `voltar em menos de 5 min retoma`() {
        val (state, closed) = engine.run(
            listOf(
                UsageEvent.Enter(IG, min(0.0)),
                UsageEvent.Leave(IG, min(3.0)),
                UsageEvent.Enter(IG, min(7.0)),   // gap de 4 min < 5 → retoma
                UsageEvent.Leave(IG, min(8.0)),
            )
        )
        assertTrue(closed.isEmpty())
        assertEquals(min(4.0), state.bankedMs)               // 3 + 1, o gap não conta
    }

    /** Ficar 5 min ou mais fora fecha o episódio e o próximo começa do zero (D3). */
    @Test
    fun `ficar 5 min fora fecha e zera`() {
        val (state, closed) = engine.run(
            listOf(
                UsageEvent.Enter(IG, min(0.0)),
                UsageEvent.Leave(IG, min(6.0)),
                UsageEvent.Enter(IG, min(12.0)),  // gap de 6 min ≥ 5 → fecha e recomeça
            )
        )
        assertEquals(1, closed.size)
        assertEquals(min(6.0), closed[0].accumulatedMs)
        assertEquals(min(0.0), closed[0].startedAt)
        assertEquals(min(6.0), closed[0].endedAt)            // fim = quando saiu
        assertEquals(EpisodePhase.DENTRO, state.phase)       // novo episódio em curso
        assertEquals(0L, state.bankedMs)
        assertEquals(min(12.0), state.episodeStartedAt)
    }

    /** Tela apagada pausa como se tivesse saído; retorno em janela retoma. */
    @Test
    fun `tela apagada pausa`() {
        val (state, closed) = engine.run(
            listOf(
                UsageEvent.Enter(IG, min(0.0)),
                UsageEvent.ScreenOff(min(5.0)),
                UsageEvent.Enter(IG, min(7.0)),   // volta em 2 min → retoma
                UsageEvent.Leave(IG, min(9.0)),
            )
        )
        assertTrue(closed.isEmpty())
        assertEquals(min(7.0), state.bankedMs)               // 5 (antes de apagar) + 2
    }

    /** RESUMED de outro alvo sem PAUSED é fronteira implícita: banca e troca, sem zerar. */
    @Test
    fun `enter de outro alvo sem leave é fronteira implicita`() {
        val (state, _) = engine.run(
            listOf(
                UsageEvent.Enter(IG, min(0.0)),
                UsageEvent.Enter(TT, min(10.0)),  // sem Leave: troca contínua
            )
        )
        assertEquals(EpisodePhase.DENTRO, state.phase)
        assertEquals(TT, state.currentApp)
        assertEquals(setOf(IG, TT), state.appsInEpisode)
        assertEquals(min(10.0), state.accumulatedMsAt(min(10.0)))
    }

    /** Fechar um episódio emite exatamente um ClosedEpisode com o acumulado final. */
    @Test
    fun `fechamento emite um ClosedEpisode`() {
        val engine = EpisodeEngine()
        var step = engine.reduce(EpisodeState.INITIAL, UsageEvent.Enter(IG, min(0.0)))
        step = engine.reduce(step.state, UsageEvent.Leave(IG, min(10.0)))
        // passa a janela sem novo evento → tick fecha
        val closeStep = engine.tick(step.state, min(16.0))
        val closed: ClosedEpisode? = closeStep.closed
        assertEquals(min(10.0), closed?.accumulatedMs)
        assertEquals(setOf(IG), closed?.apps)
        assertEquals(EpisodePhase.FORA, closeStep.state.phase)
    }

    /** Mesmo stream em duas instâncias → mesmo estado e mesmos fechados (replay, D24). */
    @Test
    fun `replay é deterministico`() {
        val events = listOf(
            UsageEvent.Enter(IG, min(0.0)),
            UsageEvent.Leave(IG, min(4.0)),
            UsageEvent.Enter(IG, min(6.0)),
            UsageEvent.Enter(TT, min(12.0)),
            UsageEvent.Leave(TT, min(20.0)),
            UsageEvent.Enter(IG, min(30.0)),  // gap 10 min → fecha o anterior
        )
        val a = EpisodeEngine().run(events)
        val b = EpisodeEngine().run(events)
        assertEquals(a.first, b.first)
        assertEquals(a.second, b.second)
    }
}
