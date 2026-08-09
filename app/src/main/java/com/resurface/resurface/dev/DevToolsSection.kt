package com.resurface.resurface.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.resurface.resurface.ui.theme.Spacing

/** Seção de ferramentas de dev (só em debug): botão que dispara um aviso de teste. */
@Composable
fun DevToolsSection(viewModel: DevToolsViewModel = hiltViewModel()) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text("Ferramentas de dev", style = MaterialTheme.typography.titleMedium)
        Button(onClick = viewModel::onTestAlert, modifier = Modifier.fillMaxWidth()) {
            Text("Disparar aviso de teste")
        }
    }
}
