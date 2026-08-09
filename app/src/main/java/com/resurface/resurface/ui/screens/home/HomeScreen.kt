package com.resurface.resurface.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.ui.theme.ResurfaceTextStyles
import com.resurface.resurface.ui.theme.ResurfaceTheme
import com.resurface.resurface.ui.theme.Spacing

/** Tela inicial: o contador vivo de tempo de vídeo curto. */
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state)
}

/** Conteúdo stateless da Home (testável em preview). */
@Composable
private fun HomeContent(state: HomeUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(Spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            if (state.active) {
                Text(
                    text = state.minutes.toString(),
                    style = ResurfaceTextStyles.statDisplay,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "minutos no ${state.appLabel}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = "em repouso",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.pausedToday) {
                Text(
                    text = "pausado por hoje",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeActivePreview() {
    ResurfaceTheme {
        HomeContent(HomeUiState(active = true, minutes = 22, appLabel = "Instagram"))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeRestPreview() {
    ResurfaceTheme {
        HomeContent(HomeUiState(active = false, pausedToday = true))
    }
}
