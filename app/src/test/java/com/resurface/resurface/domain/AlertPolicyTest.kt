package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.AlertDecision
import com.resurface.resurface.domain.model.Config
import org.junit.Assert.assertEquals
import org.junit.Test

private fun min(m: Double): Long = (m * 60_000).toLong()

class AlertPolicyTest {

    private val policy = AlertPolicy()
    private val config = Config()   // limite 20

    /** Cruzar o limite base sem aviso prévio no episódio dispara. */
    @Test
    fun `cruzou o limite dispara`() {
        val d = policy.decide(min(20.0), alertsFiredThisEpisode = 0, config, todayAlertCount = 0, pausedToday = false)
        assertEquals(AlertDecision.Fire(20), d)
    }

    /** Abaixo do limite não dispara. */
    @Test
    fun `abaixo do limite segura`() {
        val d = policy.decide(min(19.98), alertsFiredThisEpisode = 0, config, todayAlertCount = 0, pausedToday = false)
        assertEquals(AlertDecision.Hold, d)
    }

    /** O segundo aviso do episódio sai no dobro do limite (D18). */
    @Test
    fun `segundo aviso no dobro`() {
        val d = policy.decide(min(40.0), alertsFiredThisEpisode = 1, config, todayAlertCount = 1, pausedToday = false)
        assertEquals(AlertDecision.Fire(40), d)
    }

    /** Aos 39 min com um aviso já dado, ainda não cruzou o dobro (40). */
    @Test
    fun `entre o primeiro e o dobro segura`() {
        val d = policy.decide(min(39.0), alertsFiredThisEpisode = 1, config, todayAlertCount = 1, pausedToday = false)
        assertEquals(AlertDecision.Hold, d)
    }

    /** Função pura: mesma entrada, mesma decisão, sem efeito (D24). */
    @Test
    fun `é pura`() {
        val a = policy.decide(min(20.0), 0, config, 0, false)
        val b = policy.decide(min(20.0), 0, config, 0, false)
        assertEquals(a, b)
        assertEquals(AlertDecision.Fire(20), a)
    }

    /** Ao atingir o teto de 6 avisos no dia, não dispara mais (D5). */
    @Test
    fun `teto diario segura`() {
        val d = policy.decide(min(20.0), alertsFiredThisEpisode = 0, config, todayAlertCount = 6, pausedToday = false)
        assertEquals(AlertDecision.Hold, d)
    }

    /** Com a contagem diária zerada (virada da meia-noite), volta a poder disparar. */
    @Test
    fun `contagem diaria zerada permite disparar`() {
        val d = policy.decide(min(20.0), alertsFiredThisEpisode = 0, config, todayAlertCount = 0, pausedToday = false)
        assertEquals(AlertDecision.Fire(20), d)
    }

    /** "Pausar por hoje" suprime o aviso mesmo acima do limite (D11). */
    @Test
    fun `pausar por hoje suprime`() {
        val d = policy.decide(min(30.0), alertsFiredThisEpisode = 0, config, todayAlertCount = 0, pausedToday = true)
        assertEquals(AlertDecision.Hold, d)
    }

    /** Respeita um limite customizado da config. */
    @Test
    fun `respeita limite customizado`() {
        val custom = config.copy(limitMinutes = 10)
        assertEquals(AlertDecision.Fire(10), policy.decide(min(10.0), 0, custom, 0, false))
        assertEquals(AlertDecision.Hold, policy.decide(min(9.0), 0, custom, 0, false))
    }
}
