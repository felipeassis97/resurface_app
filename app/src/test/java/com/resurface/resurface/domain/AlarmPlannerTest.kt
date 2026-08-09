package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Config
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private fun min(m: Double): Long = (m * 60_000).toLong()

/** Estado DENTRO com [accMin] minutos acumulados no instante [now]. */
private fun dentro(accMin: Double, now: Long): EpisodeState = EpisodeState(
    phase = EpisodePhase.DENTRO,
    bankedMs = min(accMin),
    currentApp = "com.instagram.android",
    episodeStartedAt = 0L,
    runningSince = now,          // banked já contém tudo; trecho corrente = 0 em `now`
    pausedAt = 0L,
    appsInEpisode = setOf("com.instagram.android"),
)

class AlarmPlannerTest {

    private val planner = AlarmPlanner()
    private val config = Config()   // limite 20

    /** Aos 12 min com limite 20, o disparo é em +8 min (o restante até cruzar). */
    @Test
    fun `delay até o cruzamento`() {
        val now = 1_000_000L
        val delay = planner.nextFireDelayMs(dentro(12.0, now), config, alertsFired = 0, pausedToday = false, todayAlertCount = 0, now = now)
        assertEquals(min(8.0), delay)
    }

    /** Já além do limite, dispara agora (delay 0). */
    @Test
    fun `ja passou dispara agora`() {
        val now = 1_000_000L
        val delay = planner.nextFireDelayMs(dentro(25.0, now), config, 0, false, 0, now)
        assertEquals(0L, delay)
    }

    /** O segundo aviso mira o dobro (40 min). */
    @Test
    fun `segundo aviso mira o dobro`() {
        val now = 1_000_000L
        val delay = planner.nextFireDelayMs(dentro(30.0, now), config, alertsFired = 1, pausedToday = false, todayAlertCount = 1, now = now)
        assertEquals(min(10.0), delay)   // 40 − 30
    }

    /** Pausado por hoje → não agenda. */
    @Test
    fun `pausado nao agenda`() {
        val now = 1_000_000L
        assertNull(planner.nextFireDelayMs(dentro(12.0, now), config, 0, pausedToday = true, todayAlertCount = 0, now = now))
    }

    /** Teto diário batido → não agenda. */
    @Test
    fun `teto nao agenda`() {
        val now = 1_000_000L
        assertNull(planner.nextFireDelayMs(dentro(12.0, now), config, 0, false, todayAlertCount = 6, now = now))
    }

    /** Fora do episódio (não DENTRO) → não agenda. */
    @Test
    fun `nao dentro nao agenda`() {
        assertNull(planner.nextFireDelayMs(EpisodeState.INITIAL, config, 0, false, 0, now = 1_000_000L))
    }
}
