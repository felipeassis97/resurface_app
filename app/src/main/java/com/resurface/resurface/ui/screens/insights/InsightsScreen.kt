package com.resurface.resurface.ui.screens.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.ui.theme.ResurfaceTheme
import com.resurface.resurface.ui.theme.Spacing

/** Tela de observações: histórico de episódios e avisos com a resposta (F5, mínimo). */
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsContent(state)
}

/** Conteúdo stateless das observações. */
@Composable
private fun InsightsContent(state: InsightsUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        item {
            Text(
                text = state.eraHoraPct?.let { "$it% dos avisos: era hora" } ?: "Sem avisos respondidos ainda",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        item { SectionTitle("Avisos") }
        items(state.alerts) { alert ->
            Text(
                text = "${alert.appLabel} — ${alert.response}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item { SectionTitle("Episódios") }
        items(state.episodes) { ep ->
            Text(
                text = "${ep.durationMinutes} min · ${ep.apps}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Cabeçalho de seção. */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.space2),
    )
}

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    ResurfaceTheme {
        InsightsContent(
            InsightsUiState(
                episodes = listOf(EpisodeRow(22, "Instagram · TikTok", 0)),
                alerts = listOf(AlertRow("Instagram", "era hora", 0)),
                eraHoraPct = 75,
            )
        )
    }
}
