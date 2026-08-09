package com.resurface.resurface.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.BuildConfig
import com.resurface.resurface.ble.WristbandConnectionState
import com.resurface.resurface.domain.model.Schedule
import com.resurface.resurface.domain.model.Tone
import java.time.DayOfWeek

/** Hub de ajustes: linhas navegáveis com o valor atual + pausar por hoje. */
@Composable
fun SettingsHubScreen(
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onReminders: () -> Unit,
    onSchedule: () -> Unit,
    onWristband: () -> Unit,
    onDebug: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wristband by viewModel.wristbandState.collectAsStateWithLifecycle()

    SettingsScaffold(
        title = "Settings",
        onBack = onBack,
        bottomBar = {
            if (state.pausedToday) {
                Text(
                    "Paused for today, no alerts until tomorrow",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            } else {
                Button(onClick = viewModel::onPauseToday, modifier = Modifier.fillMaxWidth()) { Text("Pause for today") }
            }
        },
    ) {
        SettingsRow(Icons.Filled.Person, "Profile", profileSubtitle(state.name, state.hobbies.size), onProfile)
        SettingsRow(Icons.Filled.Notifications, "Reminders", "${state.limitMinutes} min · ${toneLabel(state.tone)}", onReminders)
        SettingsRow(Icons.Filled.Schedule, "Schedule", scheduleSubtitle(state.schedule), onSchedule)
        SettingsRow(Icons.Filled.Watch, "Wristband", wristbandSubtitle(wristband), onWristband)
        if (BuildConfig.DEBUG) {
            SettingsRow(Icons.Filled.BugReport, "Debug", "Developer tools", onDebug)
        }
    }
}

private fun profileSubtitle(name: String, hobbyCount: Int): String {
    val n = name.ifBlank { "No name" }
    return if (hobbyCount > 0) "$n · $hobbyCount hobbies" else n
}

private fun toneLabel(tone: Tone): String = when (tone) {
    Tone.DIRETO -> "Direct"
    Tone.GENTIL -> "Gentle"
    Tone.BEM_HUMORADO -> "Playful"
}

private val DAY_SHORT = mapOf(
    DayOfWeek.MONDAY to "Mon", DayOfWeek.TUESDAY to "Tue", DayOfWeek.WEDNESDAY to "Wed",
    DayOfWeek.THURSDAY to "Thu", DayOfWeek.FRIDAY to "Fri", DayOfWeek.SATURDAY to "Sat",
    DayOfWeek.SUNDAY to "Sun",
)

private fun scheduleSubtitle(schedule: Schedule): String {
    if (schedule.days.isEmpty()) return "Always on"
    val days = DayOfWeek.entries.filter { it in schedule.days }.joinToString(",") { DAY_SHORT.getValue(it) }
    return "$days · ${hhmm(schedule.startMinute)}–${hhmm(schedule.endMinute)}"
}

private fun wristbandSubtitle(state: WristbandConnectionState): String = when (state) {
    is WristbandConnectionState.Connected -> "Connected"
    is WristbandConnectionState.Connecting -> "Connecting…"
    WristbandConnectionState.Scanning -> "Looking…"
    else -> "Not connected"
}
