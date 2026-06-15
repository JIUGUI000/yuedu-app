package com.legado.lite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.legado.lite.ui.LegadoAppRoot
import com.legado.lite.ui.theme.LegadoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LegadoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LegadoAppRoot()
                }
            }
        }
    }
}
