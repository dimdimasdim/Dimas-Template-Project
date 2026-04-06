package com.dimas.dimasproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dimas.dimasproject.feature.randomnumber.presentation.RandomNumberScreen
import com.dimas.dimasproject.ui.theme.DimasProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DimasProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RandomNumberScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
