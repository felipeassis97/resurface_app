package com.resurface.resurface.data.config

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MidnightClockTest {

    private val zone = ZoneId.of("America/Sao_Paulo")
    private fun millis(dt: LocalDateTime) = dt.atZone(zone).toInstant().toEpochMilli()

    /** A partir de uma tarde, a próxima meia-noite é 00:00 do dia seguinte. */
    @Test
    fun `proxima meia-noite é o dia seguinte`() {
        val now = millis(LocalDateTime.of(2026, 8, 9, 15, 0))
        val expected = LocalDate.of(2026, 8, 10).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, MidnightClock(zone).nextMidnight(now))
    }

    /** Um instante logo após a meia-noite aponta pra meia-noite seguinte, não a do mesmo dia. */
    @Test
    fun `logo apos meia-noite aponta pro dia seguinte`() {
        val now = millis(LocalDateTime.of(2026, 8, 9, 0, 1))
        val expected = LocalDate.of(2026, 8, 10).atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, MidnightClock(zone).nextMidnight(now))
    }
}
