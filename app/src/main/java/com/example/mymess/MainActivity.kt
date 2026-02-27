package com.example.mymess

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mymess.ui.theme.MyMessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        com.google.firebase.FirebaseApp.initializeApp(this)

        // Initialize Repository with Context for persistence
        com.example.mymess.data.StudentRepository.init(this)

        // --- FIXED 120Hz LOGIC ---
        // This single block works for both old and new Android versions
        // by finding the highest refresh rate mode and forcing the window to use it.
        val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }

        val maxMode = currentDisplay?.supportedModes?.maxByOrNull { it.refreshRate }

        if (maxMode != null && maxMode.refreshRate > 60f) {
            val params = window.attributes
            params.preferredDisplayModeId = maxMode.modeId
            window.attributes = params
        }
        // -------------------------

        // Hiding System Bars (Immersive Mode)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            MyMessTheme {
                    MainAppNavigation()
                }
            }
        }
    }




