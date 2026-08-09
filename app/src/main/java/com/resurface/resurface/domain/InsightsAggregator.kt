package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.ClosedEpisode
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Uma resposta a um aviso, já rotulada, pra lista + S2. */
data class AlertRow(val appLabel: String, val response: String, val firedAt: Long)

/** Barra de um dia: rótulo (seg/ter…) e minutos. */
data class DayBar(val label: String, val minutes: Int)

/** Resumo da semana corrente + tendência vs a anterior. */
data class WeekSummary(
    val totalMinutes: Int = 0,
    val episodes: Int = 0,
    val avgMinutes: Int = 0,
    /** Variação % vs semana anterior; null se não há base pra comparar. */
    val trendPct: Int? = null,
)

/** Tudo que o dashboard mostra. */
data class InsightsUiState(
    val week: WeekSummary = WeekSummary(),
    val dayBars: List<DayBar> = emptyList(),
    /** 24 baldes: minutos por hora de início dos episódios da semana. */
    val hourBuckets: List<Int> = List(24) { 0 },
    val crossAppEpisodes: Int = 0,
    /** Vídeos da semana; null se não há dado de acessibilidade. */
    val videos: Int? = null,
    /** % de deslizes com hesitação; null se não há vídeos. */
    val hesitationPct: Int? = null,
    val alerts: List<AlertRow> = emptyList(),
    /** % de "era hora" entre os avisos respondidos (S2); null se nenhum respondido. */
    val eraHoraPct: Int? = null,
)

/**
 * Deriva os números do dashboard das três fontes + relógio. Puro (G1/G11): toda janela de tempo
 * é calculada com java.time e a zona injetada; sem I/O, sem Android.
 */
class InsightsAggregator {

    /** Agrega tudo pro [InsightsUiState] no instante [nowMillis], na zona [zone]. */
    fun aggregate(
        episodes: List<ClosedEpisode>,
        outcomes: List<OutcomeInput>,
        behavior: List<BehaviorInput>,
        nowMillis: Long,
        zone: ZoneId,
    ): InsightsUiState {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val weekStart = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val prevStart = today.minusDays(13).atStartOfDay(zone).toInstant().toEpochMilli()

        val thisWeek = episodes.filter { it.startedAt >= weekStart && it.startedAt <= nowMillis }
        val prevWeek = episodes.filter { it.startedAt in prevStart until weekStart }

        val totalMs = thisWeek.sumOf { it.accumulatedMs }
        val totalMin = (totalMs / 60_000L).toInt()
        val prevMin = (prevWeek.sumOf { it.accumulatedMs } / 60_000L).toInt()
        val avg = if (thisWeek.isEmpty()) 0 else totalMin / thisWeek.size
        val trend = if (prevMin == 0) null else ((totalMin - prevMin) * 100 / prevMin)

        val bars = (6 downTo 0).map { back ->
            val day = today.minusDays(back.toLong())
            val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val min = episodes.filter { it.startedAt in start until end }.sumOf { it.accumulatedMs } / 60_000L
            DayBar(label = dayLabel(day.dayOfWeek.value), minutes = min.toInt())
        }

        val buckets = MutableList(24) { 0 }
        for (ep in thisWeek) {
            val hour = Instant.ofEpochMilli(ep.startedAt).atZone(zone).hour
            buckets[hour] += (ep.accumulatedMs / 60_000L).toInt()
        }

        val crossApp = thisWeek.count { it.apps.size >= 2 }

        val weekBehavior = behavior.filter { it.timestamp >= weekStart && it.timestamp <= nowMillis }
        val videos = if (behavior.isEmpty()) null else weekBehavior.size
        val hesit = weekBehavior.count { it.hesitated }
        val hesitPct = if (videos == null || videos == 0) null else hesit * 100 / videos

        val alertRows = outcomes.map { AlertRow(it.appLabel, responseLabel(it.response), it.firedAt) }
        val responded = outcomes.filter { it.response != null }
        val eraHoraPct = if (responded.isEmpty()) null
        else responded.count { it.response == RESP_ERA_HORA } * 100 / responded.size

        return InsightsUiState(
            week = WeekSummary(totalMin, thisWeek.size, avg, trend),
            dayBars = bars,
            hourBuckets = buckets,
            crossAppEpisodes = crossApp,
            videos = videos,
            hesitationPct = hesitPct,
            alerts = alertRows,
            eraHoraPct = eraHoraPct,
        )
    }

    /** Short weekday label (1=Monday … 7=Sunday). */
    private fun dayLabel(isoDow: Int): String =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[isoDow - 1]

    /** Human label for the stored alert response. */
    private fun responseLabel(stored: String?): String = when (stored) {
        RESP_ERA_HORA -> "right time"
        RESP_AGORA_NAO -> "not now"
        else -> "no response"
    }

    private companion object {
        const val RESP_ERA_HORA = "era_hora"
        const val RESP_AGORA_NAO = "agora_nao"
    }
}

/** Entrada de outcome pro aggregator (desacopla do Room). */
data class OutcomeInput(val appLabel: String, val response: String?, val firedAt: Long)

/** Entrada de comportamento pro aggregator. */
data class BehaviorInput(val timestamp: Long, val hesitated: Boolean)
