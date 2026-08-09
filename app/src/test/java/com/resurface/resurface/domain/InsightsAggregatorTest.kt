package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.ClosedEpisode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

private const val IG = "com.instagram.android"
private const val TT = "com.zhiliaoapp.musically"

class InsightsAggregatorTest {

    private val agg = InsightsAggregator()
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = LocalDateTime.of(2026, 8, 9, 15, 0)   // domingo 15h
    private val nowMs = now.atZone(zone).toInstant().toEpochMilli()

    private fun at(dt: LocalDateTime) = dt.atZone(zone).toInstant().toEpochMilli()
    private fun ep(dt: LocalDateTime, minutes: Int, apps: Set<String> = setOf(IG)): ClosedEpisode {
        val start = at(dt)
        return ClosedEpisode(start, start + minutes * 60_000L, minutes * 60_000L, apps)
    }

    /** Total, episódios e média da semana. */
    @Test
    fun `resumo da semana`() {
        val eps = listOf(
            ep(LocalDateTime.of(2026, 8, 5, 10, 0), 10),
            ep(LocalDateTime.of(2026, 8, 8, 20, 0), 30),
        )
        val s = agg.aggregate(eps, emptyList(), emptyList(), nowMs, zone).week
        assertEquals(40, s.totalMinutes)
        assertEquals(2, s.episodes)
        assertEquals(20, s.avgMinutes)
    }

    /** Tendência vs semana anterior (40 esta, 50 anterior → −20%). */
    @Test
    fun `tendencia vs semana anterior`() {
        val eps = listOf(
            ep(LocalDateTime.of(2026, 8, 8, 20, 0), 40),
            ep(LocalDateTime.of(2026, 7, 30, 20, 0), 50),   // semana anterior
        )
        assertEquals(-20, agg.aggregate(eps, emptyList(), emptyList(), nowMs, zone).week.trendPct)
    }

    /** Minutos por dia: o dia com episódio soma; hoje (domingo) é a última barra. */
    @Test
    fun `minutos por dia`() {
        val eps = listOf(ep(LocalDateTime.of(2026, 8, 9, 10, 0), 25))
        val bars = agg.aggregate(eps, emptyList(), emptyList(), nowMs, zone).dayBars
        assertEquals(7, bars.size)
        assertEquals(25, bars.last().minutes)     // hoje
        assertEquals(0, bars.first().minutes)     // 6 dias atrás, sem uso
    }

    /** Faixa por hora: episódio começando 23h cai no balde 23. */
    @Test
    fun `faixa por hora`() {
        val eps = listOf(ep(LocalDateTime.of(2026, 8, 8, 23, 0), 15))
        val buckets = agg.aggregate(eps, emptyList(), emptyList(), nowMs, zone).hourBuckets
        assertEquals(15, buckets[23])
        assertEquals(0, buckets[10])
    }

    /** Cruza-apps: episódio com os dois pacotes conta. */
    @Test
    fun `cruza apps`() {
        val eps = listOf(
            ep(LocalDateTime.of(2026, 8, 8, 18, 0), 18, apps = setOf(IG, TT)),
            ep(LocalDateTime.of(2026, 8, 8, 20, 0), 5, apps = setOf(IG)),
        )
        assertEquals(1, agg.aggregate(eps, emptyList(), emptyList(), nowMs, zone).crossAppEpisodes)
    }

    /** Vídeos e % hesitação da semana. */
    @Test
    fun `videos e hesitacao`() {
        val beh = listOf(
            BehaviorInput(at(LocalDateTime.of(2026, 8, 8, 18, 0)), hesitated = false),
            BehaviorInput(at(LocalDateTime.of(2026, 8, 8, 18, 1)), hesitated = false),
            BehaviorInput(at(LocalDateTime.of(2026, 8, 8, 18, 2)), hesitated = false),
            BehaviorInput(at(LocalDateTime.of(2026, 8, 8, 18, 3)), hesitated = false),
            BehaviorInput(at(LocalDateTime.of(2026, 8, 8, 18, 4)), hesitated = true),
        )
        val s = agg.aggregate(emptyList(), emptyList(), beh, nowMs, zone)
        assertEquals(5, s.videos)
        assertEquals(20, s.hesitationPct)
    }

    /** Sem dado de acessibilidade → seção de vídeos nula (D15). */
    @Test
    fun `sem comportamento videos null`() {
        val s = agg.aggregate(emptyList(), emptyList(), emptyList(), nowMs, zone)
        assertNull(s.videos)
        assertNull(s.hesitationPct)
    }

    /** S2 preservada: 3 "era hora" / 1 "agora não" → 75%. */
    @Test
    fun `S2 preservada`() {
        val outs = listOf(
            OutcomeInput("Instagram", "era_hora", 1),
            OutcomeInput("Instagram", "era_hora", 2),
            OutcomeInput("Instagram", "era_hora", 3),
            OutcomeInput("Instagram", "agora_nao", 4),
            OutcomeInput("Instagram", null, 5),
        )
        assertEquals(75, agg.aggregate(emptyList(), outs, emptyList(), nowMs, zone).eraHoraPct)
    }
}
