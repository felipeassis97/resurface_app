package com.resurface.resurface.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Resurface motion — linguagem "tidal / surfacing": o conteúdo emerge, o app recolhe.
 * Rico por craft (mola, stagger, continuidade), não por volume. Honrar reduce-motion:
 * breathing → glow estático; transições → cross-fade/instantâneo.
 */
object ResurfaceMotion {
    // M3 emphasized easing set.
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    // Durations (ms).
    const val NudgeEnterMillis = 400
    const val NudgeExitMillis = 200

    // Onboarding "surfacing": entrada de conteúdo e stagger entre filhos.
    const val RiseEnterMillis = 420
    const val StaggerStepMillis = 55

    // Breathing loop (ms) — slow, calm, asymmetric inhale/exhale.
    const val BreatheInhaleMillis = 4000
    const val BreatheExhaleMillis = 6000

    /** Low-stiffness spring for the rising-tide counter and gentle component motion. */
    fun <T> tidalSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
    )

    /** Spring com leve mola pra seleção/confirmação (cards de tom, pulso). */
    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}

/**
 * Verdadeiro quando o sistema pediu para reduzir/desligar animações (Configurações de
 * acessibilidade → escala de duração de animação = 0). Fonte única de decisão de motion:
 * quando true, transições viram instantâneo/crossfade e loops (breathing) ficam estáticos.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
