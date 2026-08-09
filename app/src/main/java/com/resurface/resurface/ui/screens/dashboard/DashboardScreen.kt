package com.resurface.resurface.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.abs

/** Tela inicial única: contador vivo + observações. Top bar com acesso aos ajustes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Resurface", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { inner ->
        DashboardContent(state, Modifier.padding(inner))
    }
}

/** Conteúdo stateless do dashboard. */
@Composable
private fun DashboardContent(state: DashboardUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space6),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = Spacing.space4),
    ) {
        item { Hero(state) }
        item { WeekCard(state.insights.week, state.insights.crossAppEpisodes) }
        item { DayBars(state.insights.dayBars) }
        item { HourHeatmap(state.insights.hourBuckets) }
        if (state.insights.videos != null) {
            item { BehaviorCard(state.insights.videos!!, state.insights.hesitationPct) }
        }
        item { AlertsSection(state.insights.alerts, state.insights.eraHoraPct) }
    }
}

/** Hero adaptativo: ativo → contador vivo; ocioso → total da semana. */
@Composable
private fun Hero(state: DashboardUiState) {
    val live = state.live
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.space6)) {
        if (live.active) {
            Text(live.minutes.toString(), style = ResurfaceTextStyles.statDisplay, color = MaterialTheme.colorScheme.primary)
            Text("minutes on ${live.appLabel}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        } else {
            Text("This week", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatHm(state.insights.week.totalMinutes), style = ResurfaceTextStyles.statDisplay, color = MaterialTheme.colorScheme.primary)
            Text("at rest", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (live.pausedToday) {
            Text("paused for today", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = Spacing.space2))
        }
    }
}

/** Resumo da semana: episódios, média, tendência, cruza-apps. */
@Composable
private fun WeekCard(week: WeekSummary, crossApp: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text("This week", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${week.episodes} episodes · avg ${week.avgMinutes} min",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        week.trendPct?.let {
            val down = it <= 0
            Text(
                (if (down) "▼ " else "▲ ") + "${abs(it)}% vs last week",
                style = MaterialTheme.typography.labelLarge,
                color = if (down) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            )
        }
        if (crossApp > 0) {
            Text(
                "$crossApp episodes crossed both apps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Barras dos últimos 7 dias; o maior em âmbar, o resto neutro. */
@Composable
private fun DayBars(bars: List<DayBar>) {
    val max = (bars.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(1)
    Column {
        Text("By day", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = Spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                val isMax = bar.minutes == max && bar.minutes > 0
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((100 * bar.minutes / max).dp.coerceAtLeast(2.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isMax) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                    Text(bar.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Heatmap por hora (24 baldes): intensidade âmbar por minutos de início. */
@Composable
private fun HourHeatmap(buckets: List<Int>) {
    val max = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val full = MaterialTheme.colorScheme.primary
    Column {
        Text("By hour", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = Spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            buckets.forEach { minutes ->
                val t = minutes.toFloat() / max
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(lerp(empty, full, t)),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.space1), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("0", "6", "12", "18", "23").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Comportamento (só quando a acessibilidade dá dado). */
@Composable
private fun BehaviorCard(videos: Int, hesitationPct: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text("Behavior", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$videos videos this week", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        hesitationPct?.let {
            Text("$it% of swipes started and returned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Avisos + a razão S2. Empty state calmo (P5/P6). */
@Composable
private fun AlertsSection(alerts: List<AlertRow>, eraHoraPct: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text(
            eraHoraPct?.let { "Alerts — $it% \"right time\"" } ?: "Alerts",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (alerts.isEmpty()) {
            Text("No alerts yet — enjoy the quiet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        alerts.take(20).forEach { a ->
            Text("${a.appLabel} — ${a.response}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun formatHm(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}min" else "$minutes min"

@Preview(showBackground = true)
@Composable
private fun DashboardActivePreview() {
    ResurfaceTheme {
        DashboardContent(
            DashboardUiState(
                live = LiveState(active = true, minutes = 22, appLabel = "Instagram"),
                insights = sampleInsights(),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardIdlePreview() {
    ResurfaceTheme {
        DashboardContent(DashboardUiState(live = LiveState(active = false), insights = sampleInsights()))
    }
}

private fun sampleInsights() = InsightsUiState(
    week = WeekSummary(totalMinutes = 252, episodes = 23, avgMinutes = 11, trendPct = -18),
    dayBars = listOf(
        DayBar("Mon", 38), DayBar("Tue", 62), DayBar("Wed", 14), DayBar("Thu", 71),
        DayBar("Fri", 29), DayBar("Sat", 20), DayBar("Sun", 18),
    ),
    hourBuckets = List(24) { if (it in 22..23 || it == 0) 40 else if (it in 18..21) 15 else it % 5 },
    crossAppEpisodes = 6,
    videos = 340,
    hesitationPct = 8,
    eraHoraPct = 71,
)
