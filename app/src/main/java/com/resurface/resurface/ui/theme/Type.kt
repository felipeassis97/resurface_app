package com.resurface.resurface.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.resurface.resurface.R

/**
 * Resurface typography — three roles, deliberately separated:
 *
 *  · Voz          Bricolage Grotesque  — Display + Headline (the human voice: titles, the aviso)
 *  · Interface    Hanken Grotesk       — Title / Body / Label (labels, settings, running text)
 *  · Instrumento  Geist Mono           — numbers, time, stats (see [ResurfaceTextStyles])
 *
 * The split mirrors P2 ("só afirma o que mede"): measurements speak in the monospaced
 * instrument face; the human voice speaks in the grotesque. They never mix.
 *
 * Weights shipped (static ttf, one per weight):
 *  · Bricolage: SemiBold 600 / Bold 700 / ExtraBold 800  — never request below 600.
 *  · Hanken:    Regular 400 / Medium 500 / SemiBold 600 / Bold 700.
 *  · GeistMono: Medium 500 / SemiBold 600.
 */

val Bricolage = FontFamily(
    Font(R.font.bricolage_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_bold, FontWeight.Bold),
    Font(R.font.bricolage_extrabold, FontWeight.ExtraBold),
)

val Hanken = FontFamily(
    Font(R.font.hanken_regular, FontWeight.Normal),
    Font(R.font.hanken_medium, FontWeight.Medium),
    Font(R.font.hanken_semibold, FontWeight.SemiBold),
    Font(R.font.hanken_bold, FontWeight.Bold),
)

val GeistMono = FontFamily(
    Font(R.font.geist_mono_medium, FontWeight.Medium),
    Font(R.font.geist_mono_semibold, FontWeight.SemiBold),
)

val Typography = Typography(
    // ---- Voz (Bricolage Grotesque) ----
    displayLarge = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp, lineHeight = 60.sp, letterSpacing = (-1.0).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 45.sp, lineHeight = 50.sp, letterSpacing = (-0.75).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Bricolage, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.2).sp,
    ),
    // ---- Interface (Hanken Grotesk) ----
    titleLarge = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Hanken, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
)

/**
 * Instrumento — numeric styles in Geist Mono with tabular figures so digit width does
 * not jitter as counters tick. Use these for every live number, time, and stat instead
 * of the proportional Display/Body styles.
 */
object ResurfaceTextStyles {
    val statDisplay = TextStyle(
        fontFamily = GeistMono, fontWeight = FontWeight.SemiBold,
        fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1.0).sp,
        fontFeatureSettings = "tnum",
    )

    val statBody = TextStyle(
        fontFamily = GeistMono, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp,
        fontFeatureSettings = "tnum",
    )
}
