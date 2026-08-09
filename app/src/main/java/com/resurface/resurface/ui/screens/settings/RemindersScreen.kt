package com.resurface.resurface.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.domain.model.Tone
import com.resurface.resurface.ui.theme.Spacing

private val TONES = listOf(Tone.DIRETO to "Direct", Tone.GENTIL to "Gentle", Tone.BEM_HUMORADO to "Playful")

/** Reminders: remind time (limite) + tom. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RemindersScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var slider by remember(state.limitMinutes) { mutableFloatStateOf(state.limitMinutes.toFloat()) }
    SettingsScaffold(title = "Reminders", onBack = onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Remind me after ${slider.toInt()} minutes", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = slider,
                onValueChange = { slider = it },
                onValueChangeFinished = { viewModel.onSetLimit(slider.toInt()) },
                valueRange = 10f..60f,
                steps = 49,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("How do you want to be reminded?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                TONES.forEach { (tone, label) ->
                    FilterChip(
                        selected = state.tone == tone,
                        onClick = { viewModel.onSetTone(tone) },
                        label = { Text(label) },
                        colors = resurfaceFilterChipColors(),
                    )
                }
            }
        }
    }
}
