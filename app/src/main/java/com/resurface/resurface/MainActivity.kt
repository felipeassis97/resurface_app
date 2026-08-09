package com.resurface.resurface

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.resurface.resurface.service.MonitorService
import com.resurface.resurface.ui.ResurfaceApp
import com.resurface.resurface.ui.theme.ResurfaceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    /** Pede notificações, sobe o FGS de contexto de foreground (D-7) e monta a UI. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        startForegroundService(Intent(this, MonitorService::class.java))
        enableEdgeToEdge()
        setContent {
            ResurfaceTheme {
                ResurfaceApp()
            }
        }
    }
}
