package com.resurface.resurface.ui.screens.home

import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val IG = "com.instagram.android"

/** Estado DENTRO cujo trecho corrente começou em [runningSince], com [bankedMin] banked. */
private fun dentro(bankedMin: Double, runningSince: Long) = EpisodeState(
    phase = EpisodePhase.DENTRO,
    bankedMs = (bankedMin * 60_000).toLong(),
    currentApp = IG,
    episodeStartedAt = 0L,
    runningSince = runningSince,
    pausedAt = 0L,
    appsInEpisode = setOf(IG),
)

class HomeViewModelTest {

    /** DENTRO com 12m30s → 12 minutos, ativo, com o app. */
    @Test
    fun `dentro mostra minutos e app`() {
        val now = 1_000_000L
        val s = toHomeUiState(dentro(12.5, runningSince = now), now, paused = false)
        assertTrue(s.active)
        assertEquals(12, s.minutes)
        assertEquals("Instagram", s.appLabel)
    }

    /** O contador anda com o tempo, sem novo evento. */
    @Test
    fun `minutos sobem com o tempo`() {
        val start = 1_000_000L
        val s0 = toHomeUiState(dentro(12.0, runningSince = start), start, paused = false)
        val s1 = toHomeUiState(dentro(12.0, runningSince = start), start + 60_000, paused = false)
        assertEquals(12, s0.minutes)
        assertEquals(13, s1.minutes)   // +1 min só pelo relógio andar
    }

    /** FORA → repouso: inativo, sem minutos, sem app. */
    @Test
    fun `fora mostra repouso`() {
        val s = toHomeUiState(EpisodeState.INITIAL, 1_000_000L, paused = false)
        assertFalse(s.active)
        assertEquals(0, s.minutes)
        assertEquals("", s.appLabel)
    }

    /** Pausar por hoje é sinalizado. */
    @Test
    fun `pausa é sinalizada`() {
        val s = toHomeUiState(EpisodeState.INITIAL, 1_000_000L, paused = true)
        assertTrue(s.pausedToday)
    }
}
