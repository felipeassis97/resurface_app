package com.resurface.resurface.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.ui.theme.Spacing

private val HOBBIES = listOf("Reading", "Music", "Exercise", "Cooking", "Games", "Studying", "Friends", "Series")

/** Perfil: nome + hobbies. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScaffold(title = "Profile", onBack = onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("Your name", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onSetName,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Text("What you enjoy", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                HOBBIES.forEach { hobby ->
                    FilterChip(
                        selected = hobby in state.hobbies,
                        onClick = { viewModel.onToggleHobby(hobby) },
                        label = { Text(hobby) },
                        colors = resurfaceFilterChipColors(),
                    )
                }
            }
        }
    }
}
