package com.resurface.resurface.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.ui.theme.Spacing
import java.time.DayOfWeek

private val DAYS = listOf(
    DayOfWeek.MONDAY to "Mon", DayOfWeek.TUESDAY to "Tue", DayOfWeek.WEDNESDAY to "Wed",
    DayOfWeek.THURSDAY to "Thu", DayOfWeek.FRIDAY to "Fri", DayOfWeek.SATURDAY to "Sat",
    DayOfWeek.SUNDAY to "Sun",
)

/** Schedule: dias + janela de horário (allow-list de quando avisar). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // 0 = editando início, 1 = editando fim, null = nenhum.
    var picking by remember { mutableStateOf<Int?>(null) }

    SettingsScaffold(title = "Schedule", onBack = onBack) {
        Text("When to remind me", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            DAYS.forEach { (day, label) ->
                FilterChip(
                    selected = day in state.schedule.days,
                    onClick = { viewModel.onToggleDay(day) },
                    label = { Text(label) },
                    colors = resurfaceFilterChipColors(),
                )
            }
        }
        if (state.schedule.days.isEmpty()) {
            Text("No day selected, always on", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
                TimeField("From", state.schedule.startMinute, Modifier.weight(1f)) { picking = 0 }
                TimeField("To", state.schedule.endMinute, Modifier.weight(1f)) { picking = 1 }
            }
        }
    }

    picking?.let { which ->
        val initial = if (which == 0) state.schedule.startMinute else state.schedule.endMinute
        TimePickerDialog(
            initialMinuteOfDay = initial,
            onDismiss = { picking = null },
            onConfirm = { minuteOfDay ->
                val start = if (which == 0) minuteOfDay else state.schedule.startMinute
                val end = if (which == 1) minuteOfDay else state.schedule.endMinute
                viewModel.onSetWindow(start, end)
                picking = null
            },
        )
    }
}

/** Campo de horário: label + hora atual, abre o picker ao tocar. */
@Composable
private fun TimeField(label: String, minuteOfDay: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text("$label ${hhmm(minuteOfDay)}")
    }
}

/** Dialog com o TimePicker do Material 3 (24h). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialMinuteOfDay: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val timeState = rememberTimePickerState(
        initialHour = initialMinuteOfDay / 60,
        initialMinute = initialMinuteOfDay % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(timeState.hour * 60 + timeState.minute) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = timeState) },
    )
}
