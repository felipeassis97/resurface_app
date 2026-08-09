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

private val HOBBIES = listOf("Ler", "Música", "Exercício", "Cozinhar", "Jogos", "Estudar", "Amigos", "Séries")
private val TONES = listOf(Tone.DIRETO to "Direto", Tone.GENTIL to "Gentil", Tone.BEM_HUMORADO to "Bem-humorado")
private val DAYS = listOf(
    DayOfWeek.MONDAY to "Seg", DayOfWeek.TUESDAY to "Ter", DayOfWeek.WEDNESDAY to "Qua",
    DayOfWeek.THURSDAY to "Qui", DayOfWeek.FRIDAY to "Sex", DayOfWeek.SATURDAY to "Sáb",
    DayOfWeek.SUNDAY to "Dom",
)

/** Formata minutos-do-dia (0–1439) como "HH:MM". */
private fun hhmm(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

/** Tela de ajustes: limite, pausar por hoje, tom e hobbies (F6 + F2). */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        onSetLimit = viewModel::onSetLimit,
        onPauseToday = viewModel::onPauseToday,
        onSetTone = viewModel::onSetTone,
        onToggleHobby = viewModel::onToggleHobby,
        onToggleDay = viewModel::onToggleDay,
        onSetWindow = viewModel::onSetWindow,
        // Único ponto de contato do dev-tools na produção: some em release (BuildConfig.DEBUG).
        devTools = { if (BuildConfig.DEBUG) DevToolsSection() },
    )
}

/** Conteúdo stateless dos ajustes. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onSetLimit: (Int) -> Unit,
    onPauseToday: () -> Unit,
    onSetTone: (Tone) -> Unit,
    onToggleHobby: (String) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onSetWindow: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    devTools: @Composable () -> Unit = {},
) {
    var slider by remember(state.limitMinutes) { mutableFloatStateOf(state.limitMinutes.toFloat()) }
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.space6).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.space6),
    ) {
        // Limite
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Avisar após ${slider.toInt()} minutos", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = slider,
                onValueChange = { slider = it },
                onValueChangeFinished = { onSetLimit(slider.toInt()) },
                valueRange = 10f..60f,
                steps = 49,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Tom
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Como quer ser lembrado?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                TONES.forEach { (tone, label) ->
                    FilterChip(selected = state.tone == tone, onClick = { onSetTone(tone) }, label = { Text(label) })
                }
            }
        }

        // Hobbies
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("O que você gosta de fazer?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                HOBBIES.forEach { hobby ->
                    FilterChip(selected = hobby in state.hobbies, onClick = { onToggleHobby(hobby) }, label = { Text(hobby) })
                }
            }
        }

        // Janela ativa (allow-list): quando quero ser avisado
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Quando quero ser avisado", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                DAYS.forEach { (day, label) ->
                    FilterChip(selected = day in state.schedule.days, onClick = { onToggleDay(day) }, label = { Text(label) })
                }
            }
            if (state.schedule.days.isEmpty()) {
                Text("Sem dia marcado — avisa sempre", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
            } else {
                var start by remember(state.schedule.startMinute) { mutableFloatStateOf(state.schedule.startMinute.toFloat()) }
                var end by remember(state.schedule.endMinute) { mutableFloatStateOf(state.schedule.endMinute.toFloat()) }
                Text("Das ${hhmm(start.toInt())} às ${hhmm(end.toInt())}", style = MaterialTheme.typography.bodyMedium)
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

        // Pausar
        if (state.pausedToday) {
            Text("Pausado por hoje — sem avisos até amanhã", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
        } else {
            Button(onClick = onPauseToday, modifier = Modifier.fillMaxWidth()) { Text("Pausar por hoje") }
        }

        // Dev-tools (slot vazio em produção/preview; preenchido só em debug via SettingsScreen).
        devTools()
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    ResurfaceTheme {
        SettingsContent(
            SettingsUiState(limitMinutes = 20, tone = Tone.GENTIL, hobbies = setOf("Ler")),
            onSetLimit = {}, onPauseToday = {}, onSetTone = {}, onToggleHobby = {},
            onToggleDay = {}, onSetWindow = { _, _ -> },
        )
    }
}
