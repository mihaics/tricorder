package com.sysop.tricorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sysop.tricorder.core.ui.theme.TricorderTheme
import com.sysop.tricorder.navigation.TricorderNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TricorderTheme {
                TricorderNavHost()
            }
        }
    }
}
