package com.resurface.resurface.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Resurface color tokens — fixed Material 3 schemes.
 *
 * Direction "quiet clock": a single warm accent (âmbar) that gives a gentle tap on the
 * shoulder, over warm-biased neutrals. No second brand hue, no semantic green/red in the
 * product surface — that would read as judgment (violates P1/P5). Amber appears sparingly
 * (P6: silence is the default).
 *
 * `error` (destructive-only) is the one place red lives — reserved for "apagar histórico".
 *
 * Slot names are stable; only values change. Do NOT pair colors outside their
 * intended on-/container- pairs.
 */

// ---- Light scheme ----
val LightPrimary = Color(0xFFDE8A3B)              // âmbar
val LightOnPrimary = Color(0xFF2A1B08)
val LightPrimaryContainer = Color(0xFFF6E4CE)     // âmbar fraco
val LightOnPrimaryContainer = Color(0xFF46300E)
val LightSecondary = Color(0xFF5A5960)            // neutro (slate quente)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE6E4E0)
val LightOnSecondaryContainer = Color(0xFF201F24)
val LightTertiary = Color(0xFF5A5960)             // neutro — sem 3ª cor de marca
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFE6E4E0)
val LightOnTertiaryContainer = Color(0xFF201F24)
val LightError = Color(0xFFC6483B)                // só destrutivo
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF7D9D4)
val LightOnErrorContainer = Color(0xFF410A05)
val LightSurface = Color(0xFFF4F4F2)              // papel
val LightOnSurface = Color(0xFF17161A)            // tinta
val LightOnSurfaceVariant = Color(0xFF4C4B52)     // texto secundário legível
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)  // card
val LightSurfaceContainerLow = Color(0xFFEFEEEC)
val LightSurfaceContainer = Color(0xFFECEBE8)
val LightSurfaceContainerHigh = Color(0xFFE6E5E1)
val LightSurfaceContainerHighest = Color(0xFFE0DFDB)
val LightSurfaceDim = Color(0xFFD8D7D2)
val LightSurfaceBright = Color(0xFFFBFBFA)
val LightOutline = Color(0xFF7C7B82)
val LightOutlineVariant = Color(0xFFD6D5D1)       // hairline
val LightInverseSurface = Color(0xFF2B2A2F)
val LightInverseOnSurface = Color(0xFFF1F0EC)
val LightInversePrimary = Color(0xFFF0A64F)

// ---- Dark scheme ----
val DarkPrimary = Color(0xFFF0A64F)               // âmbar (glow)
val DarkOnPrimary = Color(0xFF3A2708)
val DarkPrimaryContainer = Color(0xFF4A320F)
val DarkOnPrimaryContainer = Color(0xFFFBDFC0)
val DarkSecondary = Color(0xFFC6C5CC)             // neutro
val DarkOnSecondary = Color(0xFF2A2930)
val DarkSecondaryContainer = Color(0xFF33323A)
val DarkOnSecondaryContainer = Color(0xFFE4E3EA)
val DarkTertiary = Color(0xFFC6C5CC)              // neutro
val DarkOnTertiary = Color(0xFF2A2930)
val DarkTertiaryContainer = Color(0xFF33323A)
val DarkOnTertiaryContainer = Color(0xFFE4E3EA)
val DarkError = Color(0xFFE0685A)                 // só destrutivo
val DarkOnError = Color(0xFF440A05)
val DarkErrorContainer = Color(0xFF7A2119)
val DarkOnErrorContainer = Color(0xFFF7D9D4)
val DarkSurface = Color(0xFF0F0F11)               // fundo (quase-preto quente, OLED)
val DarkOnSurface = Color(0xFFF3F2EE)             // tinta
val DarkOnSurfaceVariant = Color(0xFFC6C5CC)
val DarkSurfaceContainerLowest = Color(0xFF0A0A0B)
val DarkSurfaceContainerLow = Color(0xFF141417)
val DarkSurfaceContainer = Color(0xFF1A1A1E)      // superfície
val DarkSurfaceContainerHigh = Color(0xFF222227)  // elevado
val DarkSurfaceContainerHighest = Color(0xFF2B2B31)
val DarkSurfaceDim = Color(0xFF0F0F11)
val DarkSurfaceBright = Color(0xFF35353B)
val DarkOutline = Color(0xFF8E8D95)
val DarkOutlineVariant = Color(0xFF3A3A40)        // hairline
val DarkInverseSurface = Color(0xFFF3F2EE)
val DarkInverseOnSurface = Color(0xFF2B2A2F)
val DarkInversePrimary = Color(0xFFDE8A3B)

// ---- Custom: "success" role — REMAPPED TO NEUTRAL ----
// Originally a positive-reinforcement green. Reward/"you did well" contradicts P5
// (no guilt, no reward, no streaks), so the role now resolves to neutrals: it can no
// longer paint anything green. Kept as a slot only so existing call sites compile.
val LightSuccess = Color(0xFF4C4B52)
val LightOnSuccess = Color(0xFFFFFFFF)
val LightSuccessContainer = Color(0xFFECEBE8)
val LightOnSuccessContainer = Color(0xFF17161A)
val DarkSuccess = Color(0xFFC6C5CC)
val DarkOnSuccess = Color(0xFF17171B)
val DarkSuccessContainer = Color(0xFF202026)
val DarkOnSuccessContainer = Color(0xFFF3F2EE)
