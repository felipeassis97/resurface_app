package com.resurface.resurface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.resurface.resurface.ui.ResurfaceApp
import com.resurface.resurface.ui.theme.ResurfaceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Liga edge-to-edge e monta o gate (que decide onboarding vs app e sobe o FGS no app). */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ResurfaceTheme {
                ResurfaceApp()
            }
        }
    }
}
