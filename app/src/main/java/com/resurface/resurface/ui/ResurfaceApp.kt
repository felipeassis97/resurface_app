package com.resurface.resurface.ui

import androidx.compose.runtime.Composable

/**
 * App root. Por ora vai direto pro [MainShell] — o gate de onboarding (consentimento +
 * permissões) entra na F2 (ver PRODUTO.md: onboarding adiado no F1).
 */
@Composable
fun ResurfaceApp() {
    MainShell()
}
