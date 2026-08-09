package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

class ScheduleGateTest {

    private val gate = ScheduleGate()
    private val zone = ZoneId.of("America/Sao_Paulo")

    // 2024-01-01 é uma SEGUNDA; 01-02 terça; 01-08 a segunda seguinte. Sem DST em jan no BR.
    private fun at(y: Int, mo: Int, d: Int, h: Int, mi: Int): Long =
        LocalDateTime.of(y, mo, d, h, mi).atZone(zone).toInstant().toEpochMilli()

    private val mon18to23 = Schedule(setOf(DayOfWeek.MONDAY), 18 * 60, 23 * 60)
    private val monCrossMidnight = Schedule(setOf(DayOfWeek.MONDAY), 22 * 60, 1 * 60)  // 22h→01h

    /** Janela vazia é sempre ativa. */
    @Test fun `vazia sempre ativa`() {
        assertTrue(gate.isActive(Schedule(), at(2024, 1, 1, 3, 0), zone))
    }

    /** Faixa normal, dentro do horário e do dia. */
    @Test fun `faixa normal dentro`() {
        assertTrue(gate.isActive(mon18to23, at(2024, 1, 1, 20, 0), zone))
    }

    /** Fora por horário (antes do início). */
    @Test fun `faixa normal fora por horario`() {
        assertFalse(gate.isActive(mon18to23, at(2024, 1, 1, 17, 59), zone))
    }

    /** Fora por dia (terça, janela só segunda). */
    @Test fun `faixa normal fora por dia`() {
        assertFalse(gate.isActive(mon18to23, at(2024, 1, 2, 20, 0), zone))
    }

    /** Início inclusivo, fim exclusivo. */
    @Test fun `limites inicio inclusivo fim exclusivo`() {
        assertTrue(gate.isActive(mon18to23, at(2024, 1, 1, 18, 0), zone))
        assertFalse(gate.isActive(mon18to23, at(2024, 1, 1, 23, 0), zone))
    }

    /** Cruza a meia-noite: noite do dia de início ativo. */
    @Test fun `cruza meia-noite noite`() {
        assertTrue(gate.isActive(monCrossMidnight, at(2024, 1, 1, 23, 30), zone))
    }

    /** Cruza a meia-noite: madrugada do dia seguinte ativa (continuação de segunda). */
    @Test fun `cruza meia-noite madrugada`() {
        assertTrue(gate.isActive(monCrossMidnight, at(2024, 1, 2, 0, 30), zone))
    }

    /** Cruza a meia-noite: depois do fim (02h) já é inativo. */
    @Test fun `cruza meia-noite fora`() {
        assertFalse(gate.isActive(monCrossMidnight, at(2024, 1, 2, 2, 0), zone))
    }

    /** nextOpening no mesmo dia, antes de abrir. */
    @Test fun `nextOpening mesmo dia`() {
        assertEquals(at(2024, 1, 1, 18, 0), gate.nextOpening(mon18to23, at(2024, 1, 1, 17, 0), zone))
    }

    /** nextOpening pula pra próxima segunda quando a de hoje já passou. */
    @Test fun `nextOpening proxima semana`() {
        assertEquals(at(2024, 1, 8, 18, 0), gate.nextOpening(mon18to23, at(2024, 1, 1, 23, 30), zone))
    }

    /** nextOpening de janela vazia é null (sempre ativa, não precisa acordar). */
    @Test fun `nextOpening vazia null`() {
        assertNull(gate.nextOpening(Schedule(), at(2024, 1, 1, 12, 0), zone))
    }
}
