package com.resurface.resurface.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.resurface.resurface.ui.theme.ResurfaceMotion
import com.resurface.resurface.ui.theme.ResurfaceShapes
import com.resurface.resurface.ui.theme.Spacing
import com.resurface.resurface.ui.theme.rememberReducedMotion
import kotlinx.coroutines.delay

/**
 * Revela um filho "surfacing": alpha 0→1 + sobe alguns dp, com atraso escalonado por [index].
 * Sob reduced-motion, aparece instantâneo sem deslocamento.
 */
@Composable
fun RiseIn(
    index: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var shown by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            delay(index.toLong() * ResurfaceMotion.StaggerStepMillis)
            shown = true
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(ResurfaceMotion.RiseEnterMillis, easing = ResurfaceMotion.EmphasizedDecelerate),
        label = "riseIn",
    )
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 20f
        },
    ) { content() }
}

/** Indicador de progresso: segmentos; os passados/atual em âmbar, os futuros em contorno. */
@Composable
fun OnboardingProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
    ) {
        repeat(total) { i ->
            val filled = i <= current
            val color by animateColorAsState(
                targetValue = if (filled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                animationSpec = ResurfaceMotion.tidalSpring(),
                label = "progressSeg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(color, RoundedCornerShape(50)),
            )
        }
    }
}

/** Chip de status ao vivo. `granted=null` = sem estado (bateria). Concedido pulsa uma vez. */
@Composable
fun PermissionStatusChip(granted: Boolean?, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    if (granted == null) return
    val pulse by animateFloatAsState(
        targetValue = if (granted) 1f else 0f,
        animationSpec = if (reducedMotion) tween(0) else ResurfaceMotion.gentleSpring(),
        label = "grantPulse",
    )
    Row(
        modifier = modifier
            .background(
                if (granted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                ResurfaceShapes.full,
            )
            .padding(horizontal = Spacing.space3, vertical = Spacing.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
    ) {
        if (granted) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp).graphicsLayer { scaleX = pulse; scaleY = pulse },
            )
        }
        Text(
            text = if (granted) "granted" else "pending",
            style = MaterialTheme.typography.labelMedium,
            color = if (granted) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Layout base de um passo: progresso no topo, ícone contido + título (voz) + corpo, um slot de
 * conteúdo (chip/opções), e as ações no rodapé. Edge-to-edge via safeDrawing.
 */
@Composable
fun OnboardingScaffold(
    stepIndex: Int,
    icon: ImageVector,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable (reducedMotion: Boolean) -> Unit = {},
) {
    val reduced = rememberReducedMotion()
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = Spacing.space6, vertical = Spacing.space6),
        ) {
            OnboardingProgress(current = stepIndex, total = OnboardingStep.count)
            Spacer(Modifier.height(Spacing.space12))

            RiseIn(index = 0, reducedMotion = reduced) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, ResurfaceShapes.full),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.height(Spacing.space6))
            RiseIn(index = 1, reducedMotion = reduced) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(Spacing.space3))
            RiseIn(index = 2, reducedMotion = reduced) {
                Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(Spacing.space6))
            RiseIn(index = 3, reducedMotion = reduced) { content(reduced) }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(primaryLabel) }

            AnimatedVisibility(
                visible = secondaryLabel != null,
                enter = fadeIn() + scaleIn(initialScale = 0.98f),
                exit = fadeOut(),
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { onSecondary?.invoke() }) {
                        Text(secondaryLabel ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Título centralizado auxiliar (usado no welcome/done, sem ícone contido). */
@Composable
fun CenteredHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().alpha(0.9f),
    )
}
