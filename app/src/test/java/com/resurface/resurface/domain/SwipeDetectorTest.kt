package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.ScrollEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val IG = "com.instagram.android"
private const val VIEWPAGER = "androidx.viewpager.widget.ViewPager"
private const val RECYCLER = "androidx.recyclerview.widget.RecyclerView"

private fun vp(dy: Int, t: Long, dx: Int = 0, cls: String = VIEWPAGER) =
    ScrollEvent(className = cls, dy = dy, dx = dx, timestamp = t, pkg = IG)

class SwipeDetectorTest {

    private val detector = SwipeDetector()

    /** Três eventos ViewPager em ~200 ms = um vídeo (assinatura validada). */
    @Test
    fun `tres eventos viewpager viram um video`() {
        val v = detector.detect(listOf(vp(1021, 0), vp(990, 50), vp(31, 100)))
        assertEquals(1, v.size)
        assertFalse(v[0].hesitated)
    }

    /** Dois grupos separados por gap > 0,5 s = dois vídeos. */
    @Test
    fun `dois grupos por gap viram dois videos`() {
        val v = detector.detect(
            listOf(vp(500, 0), vp(300, 60), vp(20, 120), vp(478, 800), vp(1326, 860), vp(238, 920))
        )
        assertEquals(2, v.size)
    }

    /** dy negativo na rajada = hesitação (H7). */
    @Test
    fun `dy negativo é hesitacao`() {
        val v = detector.detect(listOf(vp(393, 0), vp(-323, 40), vp(-66, 80)))
        assertEquals(1, v.size)
        assertTrue(v[0].hesitated)
    }

    /** Rolar comentários (RecyclerView) não conta vídeo. */
    @Test
    fun `recyclerview nao conta`() {
        val v = detector.detect(listOf(vp(436, 0, cls = RECYCLER), vp(-863, 60, cls = RECYCLER)))
        assertTrue(v.isEmpty())
    }

    /** Swipe lateral (dx≠0, entre abas do perfil) não conta vídeo (A12). */
    @Test
    fun `swipe lateral nao conta`() {
        val v = detector.detect(listOf(vp(0, 0, dx = 1075)))
        assertTrue(v.isEmpty())
    }
}
