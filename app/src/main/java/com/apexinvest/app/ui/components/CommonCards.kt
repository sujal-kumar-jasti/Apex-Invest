package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Reuse shapes
private val CardShape = RoundedCornerShape(24.dp)

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    // Memoize colors
    val bgColor = remember(color, isDark) { color.copy(alpha = if (isDark) 0.1f else 0.05f) }
    val borderColor = remember(color) { color.copy(alpha = 0.2f) }

    // Theme integration
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(CardShape)
            .background(bgColor)
            .border(1.dp, borderColor, CardShape)
            .padding(16.dp) // Apply padding directly
    ) {
        Column {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))

            Spacer(Modifier.height(14.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = textColor,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    description: String? = null,
    isDark: Boolean = true
) {
    val bgColor = remember(color) { color.copy(alpha = 0.08f) }
    val borderColor = remember(color) { color.copy(alpha = 0.2f) }
    val iconBgColor = remember(color) { color.copy(alpha = 0.1f) }
    val textColor = remember(isDark) { if (isDark) Color.White.copy(0.8f) else Color.Black.copy(0.8f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(bgColor)
            .border(1.dp, borderColor, CardShape)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape) // Circle background
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )

                if (description != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}