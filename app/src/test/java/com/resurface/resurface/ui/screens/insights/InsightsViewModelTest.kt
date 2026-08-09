package com.resurface.resurface.ui.screens.insights

import com.resurface.resurface.data.outcome.AlertOutcomeEntity
import com.resurface.resurface.data.outcome.AlertResponse
import com.resurface.resurface.domain.model.ClosedEpisode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val IG = "com.instagram.android"
private const val TT = "com.zhiliaoapp.musically"

private fun outcome(firedAt: Long, response: String?) =
    AlertOutcomeEntity(id = firedAt, firedAt = firedAt, appLabel = "Instagram", response = response)

class InsightsViewModelTest {

    /** Episódios mantêm a ordem do repositório (mais recente primeiro) e mapeiam duração/apps. */
    @Test
    fun `episodios mapeiam duracao e apps`() {
        val eps = listOf(
            ClosedEpisode(startedAt = 5_000, endedAt = 6_000, accumulatedMs = 22 * 60_000L, apps = setOf(IG, TT)),
            ClosedEpisode(startedAt = 1_000, endedAt = 2_000, accumulatedMs = 8 * 60_000L, apps = setOf(IG)),
        )
        val s = toInsightsUiState(eps, emptyList())
        assertEquals(2, s.episodes.size)
        assertEquals(22, s.episodes[0].durationMinutes)
        assertEquals("Instagram · TikTok", s.episodes[0].apps)
        assertEquals(5_000L, s.episodes[0].startedAt)   // mais recente primeiro
    }

    /** Aviso respondido vs sem resposta. */
    @Test
    fun `avisos mostram resposta ou sem resposta`() {
        val outs = listOf(
            outcome(10, AlertResponse.ERA_HORA.stored),
            outcome(20, null),
        )
        val s = toInsightsUiState(emptyList(), outs)
        assertEquals("era hora", s.alerts[0].response)
        assertEquals("sem resposta", s.alerts[1].response)
    }

    /** S2 = 75% com 3 "era hora" e 1 "agora não" entre os respondidos (ignorados fora do denominador). */
    @Test
    fun `S2 é a razao de era hora entre respondidos`() {
        val outs = listOf(
            outcome(1, AlertResponse.ERA_HORA.stored),
            outcome(2, AlertResponse.ERA_HORA.stored),
            outcome(3, AlertResponse.ERA_HORA.stored),
            outcome(4, AlertResponse.AGORA_NAO.stored),
            outcome(5, null),   // ignorado, fora do denominador
        )
        assertEquals(75, toInsightsUiState(emptyList(), outs).eraHoraPct)
    }

    /** Sem respostas → S2 indefinida (null). */
    @Test
    fun `sem respostas S2 é null`() {
        val outs = listOf(outcome(1, null), outcome(2, null))
        assertNull(toInsightsUiState(emptyList(), outs).eraHoraPct)
    }
}
