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

/** Hobbies oferecidos (mesma lista dos ajustes). */
internal val ONBOARDING_HOBBIES =
    listOf("Reading", "Music", "Exercise", "Cooking", "Games", "Studying", "Friends", "Series")

/** Tons + exemplo de mensagem no tom. */
internal val ONBOARDING_TONES = listOf(
    Triple(Tone.DIRETO, "Direct", "22 minutes on Instagram."),
    Triple(Tone.GENTIL, "Gentle", "Hey, it has been a little while. All good?"),
    Triple(Tone.BEM_HUMORADO, "Playful", "Score: algorithm 22, you 0."),
)

/** Passo nome + tom: campo de nome no topo, depois 3 cards de tom. Nome obrigatório pra avançar. */
@Composable
fun ToneStep(
    stepIndex: Int,
    name: String,
    onName: (String) -> Unit,
    selected: Tone,
    onSelect: (Tone) -> Unit,
    canAdvance: Boolean,
    onNext: () -> Unit,
) {
    OnboardingScaffold(
        stepIndex = stepIndex,
        icon = Icons.Filled.ChatBubbleOutline,
        title = "About you",
        body = "How should we call you, and how do you want to be reminded? You can change this later in Settings.",
        primaryLabel = "Next",
        onPrimary = onNext,
        primaryEnabled = canAdvance,
    ) { reduced ->
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space4)) {
            OutlinedTextField(
                value = name,
                onValueChange = onName,
                label = { Text("Your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
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
        title = "What do you enjoy?",
        body = "Just to add texture to the reminder. It never becomes pressure. Pick at least one.",
        primaryLabel = "Next",
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
                label = { Text("Other (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Passo do limite: número grande em Geist Mono rolando + slider 10 a 60. */
@Composable
fun LimitStep(stepIndex: Int, minutes: Int, onSet: (Int) -> Unit, onNext: () -> Unit) {
    var slider by remember(minutes) { mutableFloatStateOf(minutes.toFloat()) }
    val shown = slider.toInt()
    OnboardingScaffold(
        stepIndex = stepIndex,
        icon = Icons.Filled.Timer,
        title = "Remind me after how many minutes?",
        body = "This is the first reminder threshold. Default is 20, change it anytime.",
        primaryLabel = "Next",
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
            Text("minutes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
