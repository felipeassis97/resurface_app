package com.resurface.resurface.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.domain.AlertRow
import com.resurface.resurface.domain.DayBar
import com.resurface.resurface.domain.InsightsUiState
import com.resurface.resurface.domain.WeekSummary
import com.resurface.resurface.ui.theme.ResurfaceTextStyles
import com.resurface.resurface.ui.theme.ResurfaceTheme
import com.resurface.resurface.ui.theme.Spacing

/** Dashboard de evolução: a tela que o autor abre pra acompanhar o próprio padrão. */
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsContent(state)
}

/** Conteúdo stateless do dashboard. */
@Composable
private fun InsightsContent(state: InsightsUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
    ) {
        item { WeekCard(state.week, state.crossAppEpisodes) }
        item { DayBars(state.dayBars) }
        if (state.videos != null) {
            item { BehaviorCard(state.videos, state.hesitationPct) }
        }
        item { AlertsSection(state.alerts, state.eraHoraPct) }
    }
}

/** Cartão do topo: total, episódios, média, tendência, cruza-apps. */
@Composable
private fun WeekCard(week: WeekSummary, crossApp: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text("Esta semana", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = formatHm(week.totalMinutes),
            style = ResurfaceTextStyles.statDisplay,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "${week.episodes} episódios · média ${week.avgMinutes} min",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        week.trendPct?.let {
            val down = it <= 0
            Text(
                text = (if (down) "▼ " else "▲ ") + "${kotlin.math.abs(it)}% vs semana passada",
                style = MaterialTheme.typography.labelLarge,
                color = if (down) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            )
        }
        if (crossApp > 0) {
            Text(
                text = "$crossApp episódios atravessaram os dois apps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Barras dos últimos 7 dias, altura proporcional ao maior. */
@Composable
private fun DayBars(bars: List<DayBar>) {
    val max = (bars.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(1)
    Column {
        Text("Por dia", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = Spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((100 * bar.minutes / max).dp.coerceAtLeast(2.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                    Text(bar.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Cartão de comportamento (só quando a acessibilidade dá dado). */
@Composable
private fun BehaviorCard(videos: Int, hesitationPct: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text("Comportamento", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "$videos vídeos esta semana",
            style = ResurfaceTextStyles.statBody.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        hesitationPct?.let {
            Text(
                text = "$it% dos deslizes começaram e voltaram",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Avisos + a razão S2. */
@Composable
private fun AlertsSection(alerts: List<AlertRow>, eraHoraPct: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text(
            text = eraHoraPct?.let { "Avisos — $it% \"era hora\"" } ?: "Avisos",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (alerts.isEmpty()) {
            Text(
                text = "Nenhum aviso ainda",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        alerts.take(20).forEach { a ->
            Text(
                text = "${a.appLabel} — ${a.response}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun formatHm(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}min" else "$minutes min"

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    ResurfaceTheme {
        InsightsContent(
            InsightsUiState(
                week = WeekSummary(totalMinutes = 252, episodes = 23, avgMinutes = 11, trendPct = -18),
                dayBars = listOf(
                    DayBar("seg", 38), DayBar("ter", 62), DayBar("qua", 14), DayBar("qui", 71),
                    DayBar("sex", 29), DayBar("sáb", 20), DayBar("dom", 18),
                ),
                crossAppEpisodes = 6,
                videos = 340,
                hesitationPct = 8,
                eraHoraPct = 71,
            )
        )
    }
}
