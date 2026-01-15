package com.example.absensiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.absensiapp.navigation.AppNavGraph
import com.example.absensiapp.ui.theme.AbsensiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbsensiTheme {
                AppNavGraph()
            }
        }
    }
}
