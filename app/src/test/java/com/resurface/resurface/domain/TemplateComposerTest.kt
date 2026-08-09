package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Moment
import com.resurface.resurface.domain.model.Profile
import com.resurface.resurface.domain.model.Tone
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateComposerTest {

    private val composer = TemplateComposer()
    private val moment = Moment(minutes = 22, appLabel = "Instagram", hour = 14)

    /** Preenche minutos e app nos slots. */
    @Test
    fun `preenche minutos e app`() {
        val m = composer.compose(Profile(tone = Tone.DIRETO), moment, seed = 0)
        val full = m.title + " " + m.body
        assertTrue(full.contains("22"))
        assertFalse("nenhum slot cru", full.contains("{"))
    }

    /** Sem hobby, nunca escolhe um template que precise de hobby (sem slot cru). */
    @Test
    fun `sem hobby nao usa template de hobby`() {
        for (seed in 0..30) {
            val m = composer.compose(Profile(tone = Tone.GENTIL, hobbies = emptySet()), moment, seed)
            assertFalse((m.title + m.body).contains("{hobby}"))
        }
    }

    /** Com hobby, o slot é preenchido quando o template o usa. */
    @Test
    fun `com hobby preenche o slot`() {
        // procura um seed que caia num template de hobby
        val profile = Profile(tone = Tone.GENTIL, hobbies = setOf("ler"))
        val anyHasHobby = (0..30).any { composer.compose(profile, moment, it).let { m -> (m.title + m.body).contains("ler") } }
        assertTrue("algum template de hobby foi usado e preenchido", anyHasHobby)
    }

    /** A rotação por seed varia a mensagem. */
    @Test
    fun `rotacao varia`() {
        val a = composer.compose(Profile(tone = Tone.BEM_HUMORADO), moment, seed = 0)
        val b = composer.compose(Profile(tone = Tone.BEM_HUMORADO), moment, seed = 1)
        assertTrue(a != b)
    }

    /** Nenhum template, em nenhum tom, contém padrão proibido (P2/P5) — varredura. */
    @Test
    fun `nenhum template fere P2 ou P5`() {
        val proibido = listOf("devia", "deveria", "no automático", "vidrad", "viciad", "você falhou")
        for (tone in Tone.entries) {
            for (seed in 0..40) {
                val m = composer.compose(Profile(tone = tone, hobbies = setOf("ler")), moment, seed)
                val text = (m.title + " " + m.body).lowercase()
                proibido.forEach { assertFalse("$tone: '$it' em \"$text\"", text.contains(it)) }
            }
        }
    }
}
