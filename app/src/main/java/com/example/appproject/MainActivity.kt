package com.example.appproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.appproject.ui.navigation.AppNavigationDrawer
import com.example.appproject.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase is initialized in TravelApplication
        
        // Enable Edge-to-Edge support for modern Android look
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigationDrawer()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
