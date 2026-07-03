package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.ui.theme.BrandPurple

@Composable
fun CommonScreenHeader(
    title: String? = null,
    modifier: Modifier = Modifier, // Always expose a base modifier
    onBackClick: (() -> Unit)? = null,
    centerTitle: Boolean = false,
    applyStatusBarsPadding: Boolean = true,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    // OPTIMIZATION: Completely removed 'isDark' because you weren't actually using it in this component!
    // This saves a completely useless, highly expensive luminance calculation.

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (applyStatusBarsPadding) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape) // Moved clip BEFORE background/clickable for premium ripples
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if ((leadingContent != null) || (!centerTitle && (title != null))) {
                    Spacer(Modifier.width(12.dp))
                }
            }

            if (leadingContent != null) {
                leadingContent()
            } else if (!centerTitle && title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (centerTitle && title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center),
                letterSpacing = 1.sp
            )
        }

        if (trailingContent != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trailingContent()
            }
        }
    }
}


// 1. EXTRACT BRUSH: Move the gradient outside the Composable so it is allocated in memory exactly once.
private val ProfileAvatarGradient = Brush.linearGradient(
    colors = listOf(BrandPurple, Color(0xFF9575CD))
)

@Composable
fun UserProfileHeader(
    email: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // 2. MEMOIZE STRING PARSING: String manipulation is CPU intensive.
    // This ensures it only calculates the name/initial when the email string actually changes.
    val userInitial = remember(email) {
        email.firstOrNull()?.toString()?.uppercase() ?: "A"
    }

    val displayName = remember(email) {
        if (email.contains("@")) {
            email.substringBefore("@").replaceFirstChar { it.uppercase() }
        } else {
            email.ifBlank { "Trader" }
        }
    }

    Row(
        modifier = modifier
            .clip(CircleShape) // Swapped RoundedCornerShape(50) for the static CircleShape
            .clickable(onClick = onClick)
            .padding(end = 12.dp), // Slightly increased end padding for better touch target balance
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ProfileAvatarGradient)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInitial,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = "Hello,",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}