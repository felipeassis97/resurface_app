package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageGuardTest {

    private val guard = MessageGuard()

    /** Cobrança (P5) é reprovada. */
    @Test
    fun `reprova cobranca`() {
        assertFalse(guard.isSafe(Message("22 min", "Você devia estar lendo um livro.")))
        assertFalse(guard.isSafe(Message("Larga o celular", "agora.")))
    }

    /** Afirmação de estado mental (P2) é reprovada. */
    @Test
    fun `reprova estado mental`() {
        assertFalse(guard.isSafe(Message("Você entrou no automático", "de novo.")))
        assertFalse(guard.isSafe(Message("Vidrado na tela", "há 22 min.")))
    }

    /** Texto longo demais é reprovado. */
    @Test
    fun `reprova texto longo`() {
        assertFalse(guard.isSafe(Message("t".repeat(60), "b".repeat(200))))
    }

    /** Frase segura é aprovada. */
    @Test
    fun `aprova frase segura`() {
        assertTrue(guard.isSafe(Message("22 minutos no Instagram.", "Ainda é isso que você quer estar fazendo?")))
    }
}
