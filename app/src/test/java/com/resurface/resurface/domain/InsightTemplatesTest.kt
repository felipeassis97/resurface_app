package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Tone
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightTemplatesTest {

    private val templates = InsightTemplates()

    /** Todos os tipos × tons preenchem sem slot cru e sem travessão. */
    @Test
    fun `frases limpas em todos os tipos e tons`() {
        val samples = listOf(
            Insight(InsightType.PEAK_HOUR, "peak start hour 14-15", value = 14),
            Insight(InsightType.PEAK_DAY, "heaviest day Thu", value = 71, label = "Thu"),
            Insight(InsightType.TREND, "down 18% vs last week", value = -18),
            Insight(InsightType.TREND, "up 12% vs last week", value = 12),
            Insight(InsightType.CROSS_APP, "6 cross-app sessions", value = 6),
            Insight(InsightType.VIDEOS, "340 videos this week", value = 340),
            Insight(InsightType.WELCOME, "welcome"),
        )
        for (insight in samples) {
            for (tone in Tone.entries) {
                val m = templates.phrase(insight, tone)
                val text = m.title + " " + m.body
                assertFalse("sem slot cru: $text", text.contains("{"))
                assertFalse("sem travessão: $text", text.contains("—"))
                assertTrue("título não vazio", m.title.isNotBlank())
            }
        }
    }

    /** Pico de hora usa a janela de início correta. */
    @Test
    fun `pico de hora mostra a janela`() {
        val m = templates.phrase(Insight(InsightType.PEAK_HOUR, "peak start hour 14-15", value = 14), Tone.DIRETO)
        assertTrue((m.title + m.body).contains("14h"))
    }
}
