package com.apexinvest.app.ui.screens

import android.app.Activity
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.apexinvest.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isConnected: Boolean,
    isStartupComplete: Boolean, // Kept for signature compatibility, but we will ignore it for speed
    onTasksFinished: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val isDarkMode = backgroundColor.luminance() < 0.5f
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    val systemSplashIconColor = colorResource(id = R.color.splash_icon_tint)
    val contentColor = if (isDarkMode) Color.White else Color(0xFF7A41C4)

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    var animationsFinished by remember { mutableStateOf(false) }

    // Play animations and leave immediately
    LaunchedEffect(key1 = true) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = { OvershootInterpolator(1.5f).getInterpolation(it) }
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        delay(1000) // 1 second animation minimum so it feels deliberate
        animationsFinished = true
    }

    // Trigger navigation when animation is done
    LaunchedEffect(animationsFinished) {
        if (animationsFinished) {
            onTasksFinished()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "App Logo",
                    colorFilter = ColorFilter.tint(systemSplashIconColor),
                    modifier = Modifier.size(288.dp).scale(scale.value)
                )
                Spacer(modifier = Modifier.height(16.dp))
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
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .width(200.dp)
                    .alpha(alpha.value)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(10.dp)),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}