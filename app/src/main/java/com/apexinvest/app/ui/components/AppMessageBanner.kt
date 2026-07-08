package com.apexinvest.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.apexinvest.app.ui.theme.BrandPurple
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class MessageType {
    INFO, SUCCESS, ERROR
}

@Composable
fun AppMessageBanner(
    message: String?,
    type: MessageType = MessageType.INFO,
    onDismiss: () -> Unit
) {
    // 🛠️ FIX: Latch the last known type to prevent color flicker during exit animation
    var lastType by remember { mutableStateOf(type) }
    LaunchedEffect(message, type) {
        if (message != null) {
            lastType = type
        }
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(3000.milliseconds)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val backgroundColor = when (lastType) {
            MessageType.INFO -> BrandPurple
            MessageType.SUCCESS -> Color(0xFF2E7D32) // Green
            MessageType.ERROR -> Color(0xFFD32F2F) // Red
        }

        val icon = when (lastType) {
            MessageType.INFO -> Icons.Default.Info
            MessageType.SUCCESS -> Icons.Default.CheckCircle
            MessageType.ERROR -> Icons.Default.Error
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message ?: "",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
