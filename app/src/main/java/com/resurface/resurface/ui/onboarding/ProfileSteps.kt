package com.resurface.resurface.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.resurface.resurface.domain.model.Tone
import com.resurface.resurface.ui.theme.ResurfaceMotion
import com.resurface.resurface.ui.theme.ResurfaceTextStyles
import com.resurface.resurface.ui.theme.Spacing

/** Hobbies oferecidos (mesma lista dos Ajustes). */
internal val ONBOARDING_HOBBIES =
    listOf("Ler", "Música", "Exercício", "Cozinhar", "Jogos", "Estudar", "Amigos", "Séries")

/** Tons + exemplo de mensagem no tom (F1). */
internal val ONBOARDING_TONES = listOf(
    Triple(Tone.DIRETO, "Direto", "22 minutos no Instagram."),
    Triple(Tone.GENTIL, "Gentil", "Ei — já faz um tempinho por aqui. Tudo bem?"),
    Triple(Tone.BEM_HUMORADO, "Bem-humorado", "Placar: algoritmo 22, você 0."),
)

/** Passo do tom: 3 cards selecionáveis com exemplo. */
@Composable
fun ToneStep(stepIndex: Int, selected: Tone, onSelect: (Tone) -> Unit, onNext: () -> Unit) {
    OnboardingScaffold(
        stepIndex = stepIndex,
        icon = Icons.Filled.ChatBubbleOutline,
        title = "Como quer ser lembrado?",
        body = "O aviso é escrito nesse tom. Dá pra trocar depois nos Ajustes.",
        primaryLabel = "Avançar",
        onPrimary = onNext,
    ) { reduced ->
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            ONBOARDING_TONES.forEach { (tone, label, example) ->
                ToneCard(
                    label = label,
                    example = example,
                    selected = selected == tone,
                    reducedMotion = reduced,
                    onClick = { onSelect(tone) },
                )
            }
        }
    }
}

@Composable
private fun ToneCard(
    label: String,
    example: String,
    selected: Boolean,
    reducedMotion: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.99f,
        animationSpec = if (reducedMotion) ResurfaceMotion.tidalSpring() else ResurfaceMotion.gentleSpring(),
        label = "toneScale",
    )
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(Modifier.padding(Spacing.space4), verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "“$example”",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Passo dos hobbies: múltipla escolha + campo livre. Exige ≥1 pra avançar. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HobbiesStep(
    stepIndex: Int,
    hobbies: Set<String>,
    hobbyFree: String?,
    onToggle: (String) -> Unit,
    onFree: (String) -> Unit,
    canAdvance: Boolean,
    onNext: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = stepIndex,
        icon = Icons.Filled.Favorite,
        title = "O que você gosta de fazer?",
        body = "Só pra dar textura à mensagem — nunca vira cobrança. Escolha ao menos um.",
        primaryLabel = "Avançar",
        onPrimary = onNext,
        primaryEnabled = canAdvance,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                ONBOARDING_HOBBIES.forEach { hobby ->
                    FilterChip(
                        selected = hobby in hobbies,
                        onClick = { onToggle(hobby) },
                        label = { Text(hobby) },
                    )
                }
            }
            OutlinedTextField(
                value = hobbyFree.orEmpty(),
                onValueChange = onFree,
                label = { Text("Outro (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Passo do limite: número grande em Geist Mono rolando + slider 10–60. */
@Composable
fun LimitStep(stepIndex: Int, minutes: Int, onSet: (Int) -> Unit, onNext: () -> Unit) {
    var slider by remember(minutes) { mutableFloatStateOf(minutes.toFloat()) }
    val shown = slider.toInt()
    OnboardingScaffold(
        stepIndex = stepIndex,
        icon = Icons.Filled.Timer,
        title = "Avisar depois de quantos minutos?",
        body = "É o limite do primeiro aviso. O padrão é 20; dá pra mudar quando quiser.",
        primaryLabel = "Avançar",
        onPrimary = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.space4),
        ) {
            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                AnimatedContent(
                    targetState = shown,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                        } else {
                            (slideInVertically { -it / 2 } + fadeIn()) togetherWith (slideOutVertically { it / 2 } + fadeOut())
                        }
                    },
                    label = "limitRoll",
                ) { value ->
                    Text(
                        "$value",
                        style = ResurfaceTextStyles.statDisplay,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text("minutos", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Slider(
                value = slider,
                onValueChange = { slider = it },
                onValueChangeFinished = { onSet(slider.toInt()) },
                valueRange = 10f..60f,
                steps = 49,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
