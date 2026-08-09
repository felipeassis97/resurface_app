package com.resurface.resurface.ui.screens.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.resurface.resurface.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.ui.theme.ResurfaceShapes
import com.resurface.resurface.ui.theme.Spacing
import com.resurface.resurface.ui.theme.rememberReducedMotion

/** Tela de pulseira: pareamento estilo watch (radar + estados). Ações fixas no bottom (thumb). */
@Composable
fun WristbandScreen(onBack: () -> Unit, viewModel: WristbandViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScaffold(
        title = "Wristband",
        onBack = onBack,
        bottomBar = { WristbandActions(state, viewModel) },
    ) {
        WristbandBody(state, viewModel)
    }
}

@Composable
private fun ColumnScope.WristbandBody(state: WristbandUiState, viewModel: WristbandViewModel) {
    when (val s = state) {
        WristbandUiState.Rest -> CenteredBody {
            CircledWristband()
            Text("Connect your wristband", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                "Feel the alert on your wrist, no screen needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        is WristbandUiState.Scanning -> ScanningBody(s, onConnect = viewModel::onConnect)
        WristbandUiState.Empty -> CenteredBody {
            CircledWristband()
            Text("No wristband found", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                "Turn the wristband on and bring it closer, then try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        is WristbandUiState.Connecting -> CenteredBody {
            LoadingBadge(rememberReducedMotion())
            Text(s.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text("Connecting…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        is WristbandUiState.Connected -> ConnectedBody(s, viewModel)
        is WristbandUiState.Failed -> CenteredBody {
            CircledWristband()
            Text(s.message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

/** Ações fixas no bottom, por estado. */
@Composable
private fun ColumnScope.WristbandActions(state: WristbandUiState, viewModel: WristbandViewModel) {
    when (state) {
        WristbandUiState.Rest -> Button(onClick = viewModel::onScan, modifier = Modifier.fillMaxWidth()) { Text("Scan") }
        WristbandUiState.Empty, is WristbandUiState.Failed ->
            Button(onClick = viewModel::onScan, modifier = Modifier.fillMaxWidth()) { Text("Retry") }
        is WristbandUiState.Connected -> {
            Button(onClick = viewModel::onTestPulse, modifier = Modifier.fillMaxWidth()) { Text("Send test pulse") }
            OutlinedButton(onClick = viewModel::onForget, modifier = Modifier.fillMaxWidth()) { Text("Forget") }
        }
        else -> Unit // Scanning / Connecting: sem ação fixa
    }
}

@Composable
private fun ColumnScope.ScanningBody(s: WristbandUiState.Scanning, onConnect: (String) -> Unit) {
    val reduced = rememberReducedMotion()
    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        Radar(searching = s.searching, reduced = reduced)
    }
    Text(
        if (s.searching) "Searching…" else "Tap a wristband to connect",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    s.devices.forEach { d -> DeviceItem(d, onClick = { onConnect(d.address) }) }
}

@Composable
private fun DeviceItem(d: DeviceRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ResurfaceShapes.largeIncreased)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(Spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        Image(painterResource(R.drawable.ic_wristband), contentDescription = null, modifier = Modifier.size(28.dp))
        Text(d.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        SignalBars(d.bars)
    }
}

@Composable
private fun SignalBars(bars: Int) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..4).forEach { i ->
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = (4 + i * 3).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (i <= bars) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
            )
        }
    }
}

@Composable
private fun ColumnScope.ConnectedBody(s: WristbandUiState.Connected, viewModel: WristbandViewModel) {
    var slider by remember(s.intensity) { mutableFloatStateOf((s.intensity ?: 128).toFloat()) }
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ResurfaceShapes.largeIncreased)
            .background(scheme.surfaceContainer)
            .padding(Spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        WatchBadge()
        Text(s.name, style = MaterialTheme.typography.headlineSmall, color = scheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.space1)) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
            Text("Connected", style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(if (s.intensity == null) "Intensity: automatic" else "Intensity: ${slider.toInt()}", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = slider,
            onValueChange = { slider = it },
            onValueChangeFinished = { viewModel.onSetIntensity(slider.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { viewModel.onSetIntensity(null) }) { Text("Use automatic intensity") }
    }
}

/** Radar de pareamento: anéis âmbar expandindo + ícone central. Reduced-motion → estático. */
@Composable
private fun Radar(searching: Boolean, reduced: Boolean) {
    Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Rings(animated = searching, reduced = reduced)
        CircledWristband()
    }
}

/** Loading (Connecting): ilustração num círculo + os anéis de loading em volta. */
@Composable
private fun LoadingBadge(reduced: Boolean) {
    Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Rings(animated = true, reduced = reduced)
        CircledWristband()
    }
}

/** Ilustração da pulseira dentro de um círculo (centro do radar / loading). */
@Composable
private fun CircledWristband(circle: Dp = 108.dp, icon: Dp = 64.dp) {
    Box(
        modifier = Modifier.size(circle).background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_wristband), contentDescription = null, modifier = Modifier.size(icon))
    }
}

/** Anéis concêntricos: animados (loading) ou estáticos (reduced-motion). */
@Composable
private fun Rings(animated: Boolean, reduced: Boolean) {
    if (animated && !reduced) {
        val t = rememberInfiniteTransition(label = "rings")
        repeat(3) { i ->
            val p by t.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    initialStartOffset = StartOffset(i * 800),
                ),
                label = "ring$i",
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        val sc = 0.35f + p * 0.65f
                        scaleX = sc; scaleY = sc; alpha = (1f - p) * 0.5f
                    }
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    } else {
        listOf(120.dp, 170.dp).forEach { d ->
            Box(Modifier.size(d).border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
        }
    }
}

/** Ilustração da pulseira (Vector Drawable multicolor, sem tint). */
@Composable
private fun WatchBadge(size: Dp = 96.dp) {
    Image(
        painter = painterResource(R.drawable.ic_wristband),
        contentDescription = null,
        modifier = Modifier.size(size),
    )
}

/** Corpo centralizado com respiro no topo, pros estados sem lista. */
@Composable
private fun ColumnScope.CenteredBody(content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(Spacing.space8))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
        content = content,
    )
}
