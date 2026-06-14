package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.AppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BricReViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BricReViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main layout coordinator respects standard Material design system margins
                    AppContent(viewModel = viewModel)
                }
            }
        }
    }
}
