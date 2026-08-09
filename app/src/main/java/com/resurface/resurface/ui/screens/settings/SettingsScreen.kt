package com.resurface.resurface.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.BuildConfig
import com.resurface.resurface.dev.DevToolsSection
import com.resurface.resurface.domain.model.Schedule
import com.resurface.resurface.domain.model.Tone
import com.resurface.resurface.ui.theme.ResurfaceTheme
import com.resurface.resurface.ui.theme.Spacing
import java.time.DayOfWeek

private val HOBBIES = listOf("Reading", "Music", "Exercise", "Cooking", "Games", "Studying", "Friends", "Series")
private val TONES = listOf(Tone.DIRETO to "Direct", Tone.GENTIL to "Gentle", Tone.BEM_HUMORADO to "Playful")
private val DAYS = listOf(
    DayOfWeek.MONDAY to "Mon", DayOfWeek.TUESDAY to "Tue", DayOfWeek.WEDNESDAY to "Wed",
    DayOfWeek.THURSDAY to "Thu", DayOfWeek.FRIDAY to "Fri", DayOfWeek.SATURDAY to "Sat",
    DayOfWeek.SUNDAY to "Sun",
)

/** Formata minutos-do-dia (0 a 1439) como "HH:MM". */
private fun hhmm(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

/** Tela de ajustes: nome, limite, pausar por hoje, tom, hobbies e janela (F6 + F2). */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wristbandState by viewModel.wristbandState.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        onSetName = viewModel::onSetName,
        onSetLimit = viewModel::onSetLimit,
        onPauseToday = viewModel::onPauseToday,
        onSetTone = viewModel::onSetTone,
        onToggleHobby = viewModel::onToggleHobby,
        onToggleDay = viewModel::onToggleDay,
        onSetWindow = viewModel::onSetWindow,
        wristband = {
            WristbandSettingsSection(
                state = wristbandState,
                intensity = state.intensity,
                onPair = viewModel::onPairWristband,
                onSetIntensity = viewModel::onSetIntensity,
            )
        },
        devTools = { if (BuildConfig.DEBUG) DevToolsSection() },
    )
}

/** Conteúdo stateless dos ajustes. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onSetName: (String) -> Unit,
    onSetLimit: (Int) -> Unit,
    onPauseToday: () -> Unit,
    onSetTone: (Tone) -> Unit,
    onToggleHobby: (String) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onSetWindow: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    wristband: @Composable () -> Unit = {},
    devTools: @Composable () -> Unit = {},
) {
    var slider by remember(state.limitMinutes) { mutableFloatStateOf(state.limitMinutes.toFloat()) }
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.space6).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.space6),
    ) {
        // Name
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Your name", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.name,
                onValueChange = onSetName,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Limit
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Remind me after ${slider.toInt()} minutes", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = slider,
                onValueChange = { slider = it },
                onValueChangeFinished = { onSetLimit(slider.toInt()) },
                valueRange = 10f..60f,
                steps = 49,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Tone
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("How do you want to be reminded?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                TONES.forEach { (tone, label) ->
                    FilterChip(selected = state.tone == tone, onClick = { onSetTone(tone) }, label = { Text(label) })
                }
            }
        }

        // Hobbies
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("What do you enjoy?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                HOBBIES.forEach { hobby ->
                    FilterChip(selected = hobby in state.hobbies, onClick = { onToggleHobby(hobby) }, label = { Text(hobby) })
                }
            }
        }

        // Active window (allow-list)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("When to remind me", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                DAYS.forEach { (day, label) ->
                    FilterChip(selected = day in state.schedule.days, onClick = { onToggleDay(day) }, label = { Text(label) })
                }
            }
            if (state.schedule.days.isEmpty()) {
                Text("No day selected, always on", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
            } else {
                var start by remember(state.schedule.startMinute) { mutableFloatStateOf(state.schedule.startMinute.toFloat()) }
                var end by remember(state.schedule.endMinute) { mutableFloatStateOf(state.schedule.endMinute.toFloat()) }
                Text("From ${hhmm(start.toInt())} to ${hhmm(end.toInt())}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = start, onValueChange = { start = it },
                    onValueChangeFinished = { onSetWindow(start.toInt(), end.toInt()) },
                    valueRange = 0f..1425f, steps = 94, modifier = Modifier.fillMaxWidth(),
                )
                Slider(
                    value = end, onValueChange = { end = it },
                    onValueChangeFinished = { onSetWindow(start.toInt(), end.toInt()) },
                    valueRange = 0f..1425f, steps = 94, modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Pause
        if (state.pausedToday) {
            Text("Paused for today, no alerts until tomorrow", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
        } else {
            Button(onClick = onPauseToday, modifier = Modifier.fillMaxWidth()) { Text("Pause for today") }
        }

        wristband()
        devTools()
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    ResurfaceTheme {
        SettingsContent(
            SettingsUiState(limitMinutes = 20, name = "Felipe", tone = Tone.GENTIL, hobbies = setOf("Reading")),
            onSetName = {}, onSetLimit = {}, onPauseToday = {}, onSetTone = {}, onToggleHobby = {},
            onToggleDay = {}, onSetWindow = { _, _ -> },
        )
    }
}
