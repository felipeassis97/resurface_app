package com.resurface.resurface.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.resurface.resurface.ui.theme.ResurfaceTheme
import com.resurface.resurface.ui.theme.Spacing

/** Tela de ajustes: limite de minutos e pausar por hoje (F6, mínimo). */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(state, onSetLimit = viewModel::onSetLimit, onPauseToday = viewModel::onPauseToday)
}

/** Conteúdo stateless dos ajustes. */
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onSetLimit: (Int) -> Unit,
    onPauseToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var slider by remember(state.limitMinutes) { mutableFloatStateOf(state.limitMinutes.toFloat()) }
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.space6),
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
    ) {
        Text(
            text = "Avisar após ${slider.toInt()} minutos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = slider,
            onValueChange = { slider = it },
            onValueChangeFinished = { onSetLimit(slider.toInt()) },
            valueRange = 10f..60f,
            steps = 49,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.pausedToday) {
            Text(
                text = "Pausado por hoje — sem avisos até amanhã",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        } else {
            Button(onClick = onPauseToday, modifier = Modifier.fillMaxWidth()) {
                Text("Pausar por hoje")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    ResurfaceTheme {
        SettingsContent(SettingsUiState(limitMinutes = 20), onSetLimit = {}, onPauseToday = {})
    }
}
