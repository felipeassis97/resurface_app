package com.resurface.resurface.data.usage

import com.resurface.resurface.domain.model.UsageEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val IG = "com.instagram.android"
private const val TT = "com.zhiliaoapp.musically"
private val TARGETS = setOf(IG, TT)

// Valores oficiais de UsageEvents.Event.
private const val RESUMED = 1
private const val PAUSED = 2
private const val SCREEN_NON_INTERACTIVE = 16
private const val WINDOW_STATE_CHANGED = 32

class UsageEventMapperTest {

    /** RESUMED de um alvo vira Enter. */
    @Test
    fun `resumed alvo vira enter`() {
        assertEquals(UsageEvent.Enter(IG, 100), UsageEventMapper.map(RESUMED, IG, 100, TARGETS))
    }

    /** PAUSED de um alvo vira Leave. */
    @Test
    fun `paused alvo vira leave`() {
        assertEquals(UsageEvent.Leave(TT, 200), UsageEventMapper.map(PAUSED, TT, 200, TARGETS))
    }

    /** Tela apagada vira ScreenOff, independentemente do package. */
    @Test
    fun `screen non interactive vira screenoff`() {
        assertEquals(UsageEvent.ScreenOff(300), UsageEventMapper.map(SCREEN_NON_INTERACTIVE, "qualquer", 300, TARGETS))
    }

    /** RESUMED de app fora dos alvos é descartado. */
    @Test
    fun `resumed nao alvo é descartado`() {
        assertNull(UsageEventMapper.map(RESUMED, "com.whatsapp", 100, TARGETS))
    }

    /** Tipo irrelevante é descartado. */
    @Test
    fun `tipo irrelevante é descartado`() {
        assertNull(UsageEventMapper.map(WINDOW_STATE_CHANGED, IG, 100, TARGETS))
    }
}
