package com.resurface.resurface.domain.model

import java.time.DayOfWeek

/**
 * Janela ativa (allow-list, F-janela): dias da semana marcados + faixa de horário
 * [startMinute, endMinute) em minutos do dia (0–1439). Vazia (sem dias) = sempre ativa.
 * Cruza a meia-noite quando startMinute > endMinute (ex.: 22h→01h).
 */
data class Schedule(
    val days: Set<DayOfWeek> = emptySet(),
    val startMinute: Int = DEFAULT_START,
    val endMinute: Int = DEFAULT_END,
) {
    /** Sem dia marcado → nenhuma janela configurada → sempre ativa. */
    val isEmpty: Boolean get() = days.isEmpty()

    companion object {
        const val DEFAULT_START = 18 * 60   // 18h
        const val DEFAULT_END = 23 * 60     // 23h
    }
}
