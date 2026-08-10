package com.resurface.resurface.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.domain.DayBar
import com.resurface.resurface.domain.InsightsUiState
import com.resurface.resurface.domain.WeekSummary
import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.ui.theme.ResurfaceShapes
import com.resurface.resurface.ui.theme.ResurfaceTextStyles
import com.resurface.resurface.ui.theme.ResurfaceTheme
import com.resurface.resurface.ui.theme.Spacing
import kotlin.math.abs

/** Tela inicial: header com saudação, atividade da semana em cards, gráficos. */
@Composable
fun DashboardScreen(
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { inner ->
        DashboardContent(state, onOpenSettings, Modifier.padding(inner))
    }
}

@Composable
private fun DashboardContent(state: DashboardUiState, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = Spacing.space4, bottom = Spacing.space8),
    ) {
        item { Header(state.name, onOpenSettings) }
        state.tip?.let { tip -> item { TipCard(tip) } }
        if (state.live.active) item { LiveCard(state.live) }
        item { SectionLabel("Your activity", "This week") }
        item { KpiRow(state.insights.week) }
        item { DayChartCard(state.insights.week, state.insights.dayBars) }
        item { HourCard(state.insights.hourBuckets) }
        if (state.insights.videos != null) item { BehaviorCard(state.insights.videos!!, state.insights.hesitationPct) }
    }
}

/** Header: avatar (inicial), "Welcome back" + nome, e o botão de ajustes. */
@Composable
private fun Header(name: String, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(ResurfaceShapes.full).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "R",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(Modifier.weight(1f)) {
            Text("Welcome back", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (name.isBlank()) "Hi" else name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier.size(44.dp).clip(ResurfaceShapes.full).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** Card destacado do contador vivo (só quando há episódio ativo). */
@Composable
private fun LiveCard(live: LiveState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(Spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("On ${live.appLabel} now", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (live.pausedToday) {
                Text("paused for today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Text(
            "${live.minutes}",
            style = ResurfaceTextStyles.statDisplay,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(" min", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/** Rótulo de seção + chip estático da faixa (só "This week"; não é dropdown, o dado é semanal). */
@Composable
private fun SectionLabel(title: String, range: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(
            range,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(ResurfaceShapes.full)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = Spacing.space3, vertical = Spacing.space1),
        )
    }
}

/** Dois tiles lado a lado: tempo e episódios da semana. */
@Composable
private fun KpiRow(week: WeekSummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space4), modifier = Modifier.fillMaxWidth()) {
        KpiTile(Icons.Filled.Schedule, "Time", formatHm(week.totalMinutes), Modifier.weight(1f))
        KpiTile(Icons.Filled.Bolt, "Episodes", week.episodes.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun KpiTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Box(
                modifier = Modifier.size(32.dp).clip(ResurfaceShapes.full).background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = ResurfaceTextStyles.statBody.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize), color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Card do gráfico por dia, estilo da inspiração: barra de pico em âmbar + callout. */
@Composable
private fun DayChartCard(week: WeekSummary, bars: List<DayBar>) {
    val max = (bars.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Short video by day", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            week.trendPct?.let {
                val down = it <= 0
                Text(
                    (if (down) "▼ " else "▲ ") + "${abs(it)}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (down) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                val isMax = bar.minutes == max && bar.minutes > 0
                val frac = (bar.minutes.toFloat() / max).coerceIn(0.02f, 1f)
                // Mostra o callout de tempo no pico e sempre no dia corrente.
                val showCallout = isMax || bar.isToday
                val calloutBg = if (isMax) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                val calloutFg = if (isMax) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Área das barras: o callout fica COLADO no topo da barra (não num slot fixo no topo).
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                        // Reserva no topo pro callout caber acima da barra mais alta sem cortar.
                        val reserve = 18.dp
                        val barHeight = (maxHeight - reserve) * frac
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (showCallout) {
                                Text(
                                    formatHmShort(bar.minutes),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 9.sp),
                                    color = calloutFg,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .clip(ResurfaceShapes.full)
                                        .background(calloutBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isMax) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                    ),
                            )
                        }
                    }
                    Text(
                        bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.space2),
                    )
                }
            }
        }
    }
}

/** Heatmap por hora, num card. */
@Composable
private fun HourCard(buckets: List<Int>) {
    val max = (buckets.maxOrNull() ?: 0).coerceAtLeast(1)
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val full = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Text("By hour", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(modifier = Modifier.fillMaxWidth().height(40.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            buckets.forEach { minutes ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(3.dp))
                        .background(lerp(empty, full, minutes.toFloat() / max)),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("0", "6", "12", "18", "23").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BehaviorCard(videos: Int, hesitationPct: Int?) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.surfaceContainer).padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space1),
    ) {
        Text("Behavior", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("$videos videos this week", style = ResurfaceTextStyles.statBody, color = MaterialTheme.colorScheme.onSurface)
        hesitationPct?.let {
            Text("$it% of swipes started and returned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Card de tip no topo: observação pessoal em até 2 linhas. */
@Composable
private fun TipCard(tip: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(Spacing.space4),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(ResurfaceShapes.full).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(tip.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (tip.body.isNotBlank()) {
                Text(tip.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

private fun formatHm(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"

/** Versão compacta pro callout estreito: "3h37" / "3h" / "42m". */
private fun formatHmShort(minutes: Int): String = when {
    minutes >= 60 && minutes % 60 == 0 -> "${minutes / 60}h"
    minutes >= 60 -> "${minutes / 60}h${minutes % 60}"
    else -> "${minutes}m"
}

@Preview(showBackground = true)
@Composable
private fun DashboardIdlePreview() {
    ResurfaceTheme {
        DashboardContent(
            DashboardUiState(
                name = "Felipe",
                tip = Message("You start most between 23h and 0h.", "That is your busiest window."),
                insights = sampleInsights(),
            ),
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardActivePreview() {
    ResurfaceTheme {
        DashboardContent(
            DashboardUiState(name = "Felipe", live = LiveState(active = true, minutes = 22, appLabel = "Instagram"), insights = sampleInsights()),
            onOpenSettings = {},
        )
    }
}

private fun sampleInsights() = InsightsUiState(
    week = WeekSummary(totalMinutes = 252, episodes = 23, avgMinutes = 11, trendPct = -18),
    dayBars = listOf(
        DayBar("Mon", 38), DayBar("Tue", 62), DayBar("Wed", 14), DayBar("Thu", 71),
        DayBar("Fri", 29), DayBar("Sat", 20), DayBar("Sun", 18),
    ),
    hourBuckets = List(24) { if (it in 22..23 || it == 0) 40 else if (it in 18..21) 15 else it % 5 },
    crossAppEpisodes = 6, videos = 340, hesitationPct = 8, eraHoraPct = 71,
)
