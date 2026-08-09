package com.resurface.resurface.domain

import kotlin.math.abs

/**
 * Escolhe O fato do tip a partir das estatísticas medidas (puro). Monta os candidatos que têm dado,
 * em ordem de saliência (pico de hora > tendência > dia > cruza-apps > vídeos) e alterna entre eles
 * pelo [rotationIndex]. Sem candidatos → WELCOME (tip neutro, sem número inventado, P2).
 */
class InsightSelector {

    /** Limite mínimo pra a tendência virar tip (evita ruído). */
    private val trendThreshold = 10

    fun select(state: InsightsUiState, rotationIndex: Int): Insight {
        val candidates = buildList {
            val maxHour = state.hourBuckets.maxOrNull() ?: 0
            if (maxHour > 0) {
                val h = state.hourBuckets.indexOf(maxHour)
                add(Insight(InsightType.PEAK_HOUR, "peak start hour ${h}-${(h + 1) % 24}", value = h))
            }
            state.week.trendPct?.let {
                if (abs(it) >= trendThreshold) {
                    val dir = if (it <= 0) "down" else "up"
                    add(Insight(InsightType.TREND, "$dir ${abs(it)}% vs last week", value = it))
                }
            }
            state.dayBars.maxByOrNull { it.minutes }?.let { day ->
                if (day.minutes > 0) add(Insight(InsightType.PEAK_DAY, "heaviest day ${day.label}", value = day.minutes, label = day.label))
            }
            if (state.crossAppEpisodes > 0) {
                add(Insight(InsightType.CROSS_APP, "${state.crossAppEpisodes} cross-app sessions", value = state.crossAppEpisodes))
            }
            state.videos?.let {
                if (it > 0) add(Insight(InsightType.VIDEOS, "$it videos this week", value = it))
            }
        }
        if (candidates.isEmpty()) return Insight(InsightType.WELCOME, "welcome")
        return candidates[Math.floorMod(rotationIndex, candidates.size)]
    }
}
