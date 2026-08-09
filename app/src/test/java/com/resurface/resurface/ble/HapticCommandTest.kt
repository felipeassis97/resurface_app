package com.resurface.resurface.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The encoder is the only part of the BLE stack that can be exercised without a
 * device, so it carries the coverage. Every expectation here is read off the
 * flashed firmware (README §10), not off the older 4-byte contract.
 */
class HapticCommandTest {

    private fun ByteArray.unsigned(): List<Int> = map { it.toInt() and 0xFF }

    @Test
    fun `effect only encodes a single byte`() {
        assertEquals(listOf(0x01), HapticCommand(HapticEffect.Gentle).toBytes().unsigned())
        assertEquals(listOf(0x02), HapticCommand(HapticEffect.Firm).toBytes().unsigned())
        assertEquals(listOf(0x03), HapticCommand(HapticEffect.Escalation).toBytes().unsigned())
    }

    @Test
    fun `raw effect id is placed in byte zero unchanged`() {
        assertEquals(listOf(0x04), HapticCommand(HapticEffect.Raw(0x04)).toBytes().unsigned())
        assertEquals(listOf(0x7F), HapticCommand(HapticEffect.Raw(0x7F)).toBytes().unsigned())
    }

    @Test
    fun `effect and intensity encode two bytes`() {
        val bytes = HapticCommand(HapticEffect.Gentle, intensity = 128).toBytes()
        assertEquals(listOf(0x01, 0x80), bytes.unsigned())
    }

    @Test
    fun `effect intensity and duration encode three bytes`() {
        val bytes = HapticCommand(
            HapticEffect.Gentle,
            intensity = 128,
            durationMillis = 500,
        ).toBytes()
        assertEquals(listOf(0x01, 0x80, 0x32), bytes.unsigned())
    }

    @Test
    fun `duration without intensity pads byte one with zero`() {
        // Byte 2 cannot exist without byte 1 — the firmware reads positionally, and
        // intensity 0 is its "use the default" signal.
        val bytes = HapticCommand(HapticEffect.Gentle, durationMillis = 100).toBytes()
        assertEquals(listOf(0x01, 0x00, 0x0A), bytes.unsigned())
    }

    @Test
    fun `duration converts from milliseconds to ten millisecond units`() {
        fun durationByte(millis: Int) =
            HapticCommand(HapticEffect.Raw(0x04), intensity = 64, durationMillis = millis)
                .toBytes().unsigned()[2]

        assertEquals(10, durationByte(100))
        assertEquals(50, durationByte(500))
        assertEquals(100, durationByte(1000))
        assertEquals(255, durationByte(2550))
    }

    @Test
    fun `duration above the firmware ceiling is clamped not wrapped`() {
        fun durationByte(millis: Int) =
            HapticCommand(HapticEffect.Gentle, durationMillis = millis).toBytes().unsigned()[2]

        assertEquals(255, durationByte(2551))
        assertEquals(255, durationByte(10_000))
        assertEquals(255, durationByte(Int.MAX_VALUE))
    }

    @Test
    fun `every encoded byte stays within the unsigned byte range`() {
        val awkward = listOf(-500, 0, 1, 2549, 2550, 2551, 60_000, Int.MAX_VALUE)
        for (millis in awkward) {
            for (intensity in listOf(-10, 0, 1, 40, 255, 256, 4096)) {
                val bytes = HapticCommand(
                    HapticEffect.Gentle,
                    intensity = intensity,
                    durationMillis = millis,
                ).toBytes()
                assertTrue(
                    "millis=$millis intensity=$intensity produced ${bytes.unsigned()}",
                    bytes.unsigned().all { it in 0..255 },
                )
            }
        }
    }

    @Test
    fun `sub-ten-millisecond duration still encodes a playable value`() {
        // Rounding to zero would silently turn an explicit duration into "use the default".
        val bytes = HapticCommand(HapticEffect.Gentle, durationMillis = 5).toBytes()
        assertEquals(1, bytes.unsigned()[2])
    }

    @Test
    fun `intensity is clamped to the unsigned byte range`() {
        assertEquals(255, HapticCommand(HapticEffect.Gentle, intensity = 4096).toBytes().unsigned()[1])
        assertEquals(0, HapticCommand(HapticEffect.Gentle, intensity = -1).toBytes().unsigned()[1])
    }

    @Test
    fun `payload never exceeds three bytes`() {
        val bytes = HapticCommand(
            HapticEffect.Escalation,
            intensity = 255,
            durationMillis = 2550,
        ).toBytes()
        assertEquals(3, bytes.size)
    }

    // --- firmware playback timing (2.4) ---

    @Test
    fun `gentle blocking time follows the requested duration`() {
        assertEquals(150, HapticCommand(HapticEffect.Gentle).firmwareBlockingMillis())
        assertEquals(
            500,
            HapticCommand(HapticEffect.Gentle, durationMillis = 500).firmwareBlockingMillis(),
        )
    }

    @Test
    fun `raw blocking time follows the requested duration with its own default`() {
        assertEquals(200, HapticCommand(HapticEffect.Raw(0x04)).firmwareBlockingMillis())
        assertEquals(
            1000,
            HapticCommand(HapticEffect.Raw(0x04), durationMillis = 1000).firmwareBlockingMillis(),
        )
    }

    @Test
    fun `firm blocking time is fixed regardless of parameters`() {
        val expected = 420 // 150 + 120 + 150
        assertEquals(expected, HapticCommand(HapticEffect.Firm).firmwareBlockingMillis())
        assertEquals(
            expected,
            HapticCommand(HapticEffect.Firm, intensity = 100, durationMillis = 2000)
                .firmwareBlockingMillis(),
        )
    }

    @Test
    fun `escalation blocking time is fixed regardless of parameters`() {
        val expected = 1100 // 44 steps x 25 ms
        assertEquals(expected, HapticCommand(HapticEffect.Escalation).firmwareBlockingMillis())
        assertEquals(
            expected,
            HapticCommand(HapticEffect.Escalation, intensity = 10, durationMillis = 50)
                .firmwareBlockingMillis(),
        )
    }

    @Test
    fun `blocking time reflects the clamped duration not the requested one`() {
        assertEquals(
            2550,
            HapticCommand(HapticEffect.Gentle, durationMillis = 10_000).firmwareBlockingMillis(),
        )
    }
}
