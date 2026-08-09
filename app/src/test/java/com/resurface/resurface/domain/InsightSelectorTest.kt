package com.resurface.resurface.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightSelectorTest {

    private val selector = InsightSelector()

    private fun state(
        hours: List<Int> = List(24) { 0 },
        days: List<DayBar> = emptyList(),
        trend: Int? = null,
        crossApp: Int = 0,
        videos: Int? = null,
    ) = InsightsUiState(
        week = WeekSummary(trendPct = trend),
        dayBars = days,
        hourBuckets = hours,
        crossAppEpisodes = crossApp,
        videos = videos,
    )

    /** Sem dado nenhum → WELCOME (não inventa número). */
    @Test
    fun `sem dado vai pro welcome`() {
        assertEquals(InsightType.WELCOME, selector.select(state(), 0).type)
    }

    /** Pico de hora vira PEAK_HOUR com a hora certa. */
    @Test
    fun `pico de hora`() {
        val hours = List(24) { if (it == 14) 40 else 0 }
        val insight = selector.select(state(hours = hours), 0)
        assertEquals(InsightType.PEAK_HOUR, insight.type)
        assertEquals(14, insight.value)
    }

    /** Tendência abaixo do limiar não entra; peak hour ganha (mais saliente). */
    @Test
    fun `tendencia fraca ignorada`() {
        val hours = List(24) { if (it == 9) 30 else 0 }
        val insight = selector.select(state(hours = hours, trend = 5), 0)
        assertEquals(InsightType.PEAK_HOUR, insight.type)
    }

    /** Rotação alterna entre os candidatos disponíveis. */
    @Test
    fun `rotacao alterna`() {
        val s = state(
            hours = List(24) { if (it == 20) 50 else 0 },
            days = listOf(DayBar("Mon", 10), DayBar("Tue", 80)),
            crossApp = 3,
        )
        val a = selector.select(s, 0).type
        val b = selector.select(s, 1).type
        val c = selector.select(s, 2).type
        assertTrue("candidatos distintos por rotação", setOf(a, b, c).size >= 2)
        // volta ao início (3 candidatos)
        assertEquals(a, selector.select(s, 3).type)
    }
}
