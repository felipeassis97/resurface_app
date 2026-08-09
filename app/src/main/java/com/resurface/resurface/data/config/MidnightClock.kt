package com.resurface.resurface.data.config

import java.time.Instant
import java.time.ZoneId

/** Calcula a próxima meia-noite no fuso dado — usado por "pausar por hoje" (D-6). */
class MidnightClock(private val zone: ZoneId = ZoneId.systemDefault()) {

    /** Epoch millis da próxima meia-noite (00:00 do dia seguinte) a partir de [nowMillis]. */
    fun nextMidnight(nowMillis: Long): Long =
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Epoch millis do início do dia (00:00 de hoje) — pro teto diário de avisos. */
    fun startOfDay(nowMillis: Long): Long =
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()
}
