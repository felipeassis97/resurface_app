package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Schedule
import java.time.Instant
import java.time.ZoneId

/**
 * Decide se um instante cai dentro da janela ativa (puro, testável sem relógio real).
 * Janela vazia = sempre ativo. Trata faixa normal e faixa que cruza a meia-noite.
 */
class ScheduleGate {

    /** Verdadeiro se [now] está dentro da janela no fuso [zone]. Vazia → sempre true. */
    fun isActive(schedule: Schedule, now: Long, zone: ZoneId): Boolean {
        if (schedule.isEmpty) return true
        val dt = Instant.ofEpochMilli(now).atZone(zone)
        val minute = dt.hour * 60 + dt.minute
        val day = dt.dayOfWeek
        return if (schedule.startMinute <= schedule.endMinute) {
            // Faixa normal, no mesmo dia.
            day in schedule.days && minute >= schedule.startMinute && minute < schedule.endMinute
        } else {
            // Cruza a meia-noite: noite do dia de início OU madrugada do dia seguinte.
            (day in schedule.days && minute >= schedule.startMinute) ||
                (day.minus(1) in schedule.days && minute < schedule.endMinute)
        }
    }

    /** Próximo instante (> [now]) em que a janela abre; null se sempre ativa (vazia). */
    fun nextOpening(schedule: Schedule, now: Long, zone: ZoneId): Long? {
        if (schedule.isEmpty) return null
        val base = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        // Varre os próximos 8 dias procurando o primeiro início de janela depois de agora.
        for (i in 0..8) {
            val date = base.plusDays(i.toLong())
            if (date.dayOfWeek !in schedule.days) continue
            val openMs = date.atStartOfDay(zone).plusMinutes(schedule.startMinute.toLong())
                .toInstant().toEpochMilli()
            if (openMs > now) return openMs
        }
        return null
    }
}
