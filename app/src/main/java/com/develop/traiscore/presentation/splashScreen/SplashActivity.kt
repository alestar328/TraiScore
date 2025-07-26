package com.develop.traiscore.presentation.splashScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.develop.traiscore.presentation.MainActivity
import com.develop.traiscore.presentation.theme.TraiScoreTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TraiScoreTheme {
                SplashScreen {
                    // Navegar a MainActivity después del splash
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}