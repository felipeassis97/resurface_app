package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.AlertDecision
import com.resurface.resurface.domain.model.Config
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.UsageEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val IG = "com.instagram.android"
private const val TT = "com.zhiliaoapp.musically"

/** Segundos desde o início do episódio (17:45:58) → millis. */
private fun s(sec: Long): Long = sec * 1000L

/**
 * Golden test do episódio real de 2026-08-08 (PRODUTO §5.5): Instagram + TikTok fundidos num
 * episódio só, depois 15 min fora abrindo um segundo.
 *
 * O resumo textual do §5.5 é ilustrativo (omite sub-pausas): as timestamps dele, somadas pelo
 * motor, dão ~21:42, enquanto o medido real foi 18:43 (uma quase-batida, faltou 1:17 pro aviso).
 * Por isso o golden valida a MECÂNICA (fusão entre apps, pausa < 5 min, fechamento após 15 min),
 * não o número exato — ver design.md D6.
 */
class EpisodeGoldenTest {

    // Offsets em segundos a partir de 17:45:58 (as timestamps RESUMED/PAUSED do §5.5).
    private val timeline = listOf(
        UsageEvent.Enter(IG, s(0)),      // 17:45:58 entra
        UsageEvent.Leave(IG, s(110)),    // 17:47:48 sai
        UsageEvent.Enter(IG, s(329)),    // 17:51:27 volta (gap 3:39)
        UsageEvent.Leave(IG, s(436)),    // 17:53:14
        UsageEvent.Enter(IG, s(672)),    // 17:57:10 volta (gap 3:56)
        UsageEvent.Leave(IG, s(864)),    // 18:00:22
        UsageEvent.Enter(IG, s(1089)),   // 18:04:07 volta (gap 3:45)
        UsageEvent.Leave(IG, s(1471)),   // 18:10:29
        UsageEvent.Enter(TT, s(1667)),   // 18:13:45 TROCA pro TikTok (gap 3:16)
        UsageEvent.Leave(TT, s(2178)),   // 18:22:16
        UsageEvent.Enter(IG, s(3116)),   // 18:37:54 volta após 15:38 → fecha e reabre
    )

    /** A timeline fecha exatamente um episódio, atravessando Instagram E TikTok (D2/D14). */
    @Test
    fun `um unico episodio atravessa os dois apps`() {
        val (_, closed) = EpisodeEngine().run(timeline)
        assertEquals(1, closed.size)
        assertEquals(setOf(IG, TT), closed[0].apps)
        assertEquals(s(0), closed[0].startedAt)
        assertEquals(s(2178), closed[0].endedAt)          // fim = saída do TikTok
    }

    /** O acumulado é da ordem de ~18–22 min (plausível) e determinístico no fixture. */
    @Test
    fun `acumulado plausivel e deterministico`() {
        val (_, closed) = EpisodeEngine().run(timeline)
        val acc = closed[0].accumulatedMs
        assertTrue("≥ 18 min", acc >= 18 * 60_000L)
        assertEquals(s(1302), acc)                         // soma dos trechos do §5.5 = 21:42
    }

    /** Após o gap de 15:38 (≥5 min), um segundo episódio começa do zero (D3). */
    @Test
    fun `segundo episodio comeca apos o gap de 15 min`() {
        val (state, _) = EpisodeEngine().run(timeline)
        assertEquals(EpisodePhase.DENTRO, state.phase)
        assertEquals(s(3116), state.episodeStartedAt)
        assertEquals(0L, state.bankedMs)
    }

    /** Sobre o acumulado fundido, a AlertPolicy decide de forma determinística (limite 20). */
    @Test
    fun `politica decide sobre o acumulado fundido`() {
        val (_, closed) = EpisodeEngine().run(timeline)
        // Neste fixture (21:42) o limite de 20 é cruzado → Fire. (No real 18:43 teria sido near-miss.)
        val decision = AlertPolicy().decide(
            accumulatedMs = closed[0].accumulatedMs,
            alertsFiredThisEpisode = 0,
            config = Config(),
            todayAlertCount = 0,
            pausedToday = false,
        )
        assertEquals(AlertDecision.Fire(20), decision)
    }
}
