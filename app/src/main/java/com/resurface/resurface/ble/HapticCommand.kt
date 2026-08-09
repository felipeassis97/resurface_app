package com.resurface.resurface.ble

/**
 * A firmware effect. [id] is byte 0 of the payload.
 *
 * [honoursIntensity] / [honoursDuration] record what the *firmware* does with the
 * remaining bytes — Firm and Escalation read them off the wire and then ignore
 * them, so the UI must not imply the sliders do anything for those effects.
 */
sealed interface HapticEffect {
    val id: Int
    val label: String
    val honoursIntensity: Boolean
    val honoursDuration: Boolean

    /** One short pulse. Defaults: intensity 120, 150 ms. */
    data object Gentle : HapticEffect {
        override val id = 0x01
        override val label = "Gentle"
        override val honoursIntensity = true
        override val honoursDuration = true
    }

    /** Two strong pulses. Duration is fixed at 150 ms per pulse. */
    data object Firm : HapticEffect {
        override val id = 0x02
        override val label = "Firm"
        override val honoursIntensity = true
        override val honoursDuration = false
    }

    /** A PWM ramp from 40 to 255. Both parameters are ignored. */
    data object Escalation : HapticEffect {
        override val id = 0x03
        override val label = "Escalation"
        override val honoursIntensity = false
        override val honoursDuration = false
    }

    /** Any other id — falls into the firmware's default branch. Defaults: 200 ms. */
    data class Raw(override val id: Int) : HapticEffect {
        override val label = "Raw 0x%02X".format(id)
        override val honoursIntensity = true
        override val honoursDuration = true
    }

    companion object {
        val named: List<HapticEffect> = listOf(Gentle, Firm, Escalation)
    }
}

/**
 * A command for the wristband, encodable to the 1–3 byte payload firmware v3 expects
 * (README §10).
 *
 * A null [intensity] or [durationMillis] means "omit the byte", which the firmware
 * reads as "use my default". Note that an *explicit* zero means the same thing —
 * there is no payload that stops the motor.
 */
data class HapticCommand(
    val effect: HapticEffect,
    val intensity: Int? = null,
    val durationMillis: Int? = null,
) {

    /**
     * Byte 2 in the firmware's 10 ms unit, clamped to a playable value. Null when no
     * duration was requested.
     *
     * Clamped to at least 1: a sub-10 ms request rounding down to 0 would silently
     * become "use the firmware default", which is not what the caller asked for.
     */
    private val durationUnits: Int? =
        durationMillis?.let { millis ->
            // Widened: rounding up an Int near MAX_VALUE overflows to negative and the
            // clamp would then read it as the *shortest* duration instead of the longest.
            val units = (millis.toLong() + MILLIS_PER_UNIT - 1) / MILLIS_PER_UNIT
            units.coerceIn(1L, MAX_DURATION_UNITS.toLong()).toInt()
        }

    /** The payload as the wristband will receive it. Pure — this is the tested core. */
    fun toBytes(): ByteArray {
        val effectByte = effect.id.toByte()
        // The firmware reads positionally, so a duration forces an intensity byte;
        // 0 there falls back to the effect's default intensity.
        val intensityByte = when {
            intensity != null -> intensity.coerceIn(0, MAX_BYTE).toByte()
            durationUnits != null -> 0
            else -> null
        }
        return when {
            intensityByte == null -> byteArrayOf(effectByte)
            durationUnits == null -> byteArrayOf(effectByte, intensityByte)
            else -> byteArrayOf(effectByte, intensityByte, durationUnits.toByte())
        }
    }

    /**
     * How long the firmware's `loop()` blocks playing this command.
     *
     * Callers must space commands by at least this much: playback uses `delay()`, and
     * a write arriving mid-effect overwrites the single pending-command slot rather
     * than queueing behind it — which looks exactly like a dropped write and is not.
     */
    fun firmwareBlockingMillis(): Int = when (effect) {
        HapticEffect.Firm -> FIRM_BLOCKING_MILLIS
        HapticEffect.Escalation -> ESCALATION_BLOCKING_MILLIS
        HapticEffect.Gentle -> durationUnits?.times(MILLIS_PER_UNIT) ?: GENTLE_DEFAULT_MILLIS
        is HapticEffect.Raw -> durationUnits?.times(MILLIS_PER_UNIT) ?: RAW_DEFAULT_MILLIS
    }

    /** The payload rendered for the debug console's preview, e.g. `01 80 32`. */
    fun toHexPreview(): String =
        toBytes().joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }

    companion object {
        const val MILLIS_PER_UNIT = 10
        const val MAX_BYTE = 255
        const val MAX_DURATION_UNITS = 255
        const val MAX_DURATION_MILLIS = MAX_DURATION_UNITS * MILLIS_PER_UNIT // 2550

        /**
         * An ERM has a start voltage; below roughly this PWM value the motor may not
         * spin at all. The firmware's own escalation ramp starts here.
         */
        const val MOTOR_START_THRESHOLD = 40

        private const val GENTLE_DEFAULT_MILLIS = 150
        private const val RAW_DEFAULT_MILLIS = 200
        private const val FIRM_BLOCKING_MILLIS = 150 + 120 + 150
        private const val ESCALATION_BLOCKING_MILLIS = 44 * 25 // steps 40..255 by 5, 25 ms each
    }
}
