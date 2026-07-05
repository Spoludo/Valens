package com.spoludo.valens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spoludo.valens.ui.home.HomeScreen
import com.spoludo.valens.ui.theme.ValensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ValensTheme {
                HomeScreen()
            }
        }
    }
}
