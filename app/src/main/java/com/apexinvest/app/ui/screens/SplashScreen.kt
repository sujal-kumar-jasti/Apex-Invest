package com.apexinvest.app.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(isConnected: Boolean) {
    // 1. Theme Detection Logic
    val isDarkMode = isSystemInDarkTheme()

    // 2. Fetch the dynamic tint from your XML resource
    val splashTint = colorResource(id = R.color.splash_icon_tint)

    // 3. Define the UI colors based on your rules
    // Rule: Background white in light mode, black in dark mode
    val backgroundColor = if (isDarkMode) Color.Black else Color.White

    // Rule: Text and Progress should be white in dark mode, brand purple in light mode
    val contentColor = if (isDarkMode) Color.White else Color(0xFF533D85)

    // Animation States
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Logo Bounce Animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = { OvershootInterpolator(1.5f).getInterpolation(it) }
            )
        )
        // Text/Progress Fade In
        delay(200)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }
    Scaffold(
        // Dynamic Insets: 0dp when Offline (Banner pushes us down), Default when Online
        contentWindowInsets = if (!isConnected) {
            WindowInsets(0.dp)
        } else {
            ScaffoldDefaults.contentWindowInsets
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- LOGO ---
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "App Logo",
                    // Using the specific XML color resource for the tint
                    colorFilter = ColorFilter.tint(splashTint),
                    modifier = Modifier
                        .size(288.dp) // Exact size for seamless transition
                        .scale(scale.value)
                )

                // No height needed here if the XML has the padding we added earlier
                Spacer(modifier = Modifier.height(0.dp))

                // --- APP NAME ---
                Text(
                    text = "ApexInvest",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.alpha(alpha.value),
                    fontSize = 32.sp
                )

                Text(
                    text = "Smart Investment Intelligence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.alpha(alpha.value)
                )
            }

            // --- LOADING INDICATOR ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .width(200.dp)
                    .alpha(alpha.value)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}