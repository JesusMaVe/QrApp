package com.example.qrapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.qrapplication.navigation.AppNavHost
import com.example.qrapplication.ui.theme.QrApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QrApplicationTheme {
                AppNavHost()
            }
        }
    }
}
