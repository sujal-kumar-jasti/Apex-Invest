package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.toCleanString

@Composable
fun CommonStockRow(
    symbol: String,
    companyName: String,
    price: Double,
    percentChange: Double,
    isUsd: Boolean,
    modifier: Modifier = Modifier, // Always expose a modifier for parent control
    historyData: List<Double> = emptyList(), // Tip: Use ImmutableList here if possible
    isDark: Boolean = true, // Removed luminance() default to save calculation time
    quantity: Double? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    // 1. MEMOIZE CALCULATIONS: Prevent recalculating strings/colors on every scroll frame
    val isUp = remember(percentChange) { percentChange >= 0 }
    val trendColor = remember(isUp, appColors) { if (isUp) appColors.trendGreen else appColors.trendRed }
    val trendBg = remember(trendColor) { trendColor.copy(alpha = 0.15f) }
    val currencySymbol = remember(isUsd) { getCurrencySymbol(if (isUsd) "USD" else "INR") }
    val quantityText = remember(quantity) { quantity?.let { " • ${it.toCleanString()} Qty" } }

    // 2. FLATTEN LAYOUT: Removed the outer Box. The Row can handle the background and clicks.
    Row(
        modifier = modifier
            .fillMaxWidth()
            // IMPORTANT: If 'glassCard' causes lag, replace it with a solid surface/shadow for lists.
            .glassCard(isDark, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)) // Clips the ripple effect cleanly to the borders
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left Section: Ticker & Name ---
        Column(
            modifier = Modifier
                .weight(1.3f)
                .padding(end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = symbol,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (quantityText != null) {
                    Text(
                        text = quantityText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Text(
                text = companyName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // --- Middle Section: Sparkline ---
        if (historyData.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(35.dp)
                    .padding(horizontal = 8.dp)
            ) {
                AppSparkline(
                    data = historyData,
                    color = trendColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Empty spacer to maintain layout balance if there's no chart data
            Spacer(modifier = Modifier.weight(0.9f))
        }

        // --- Right Section: Price & Change ---
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.wrapContentWidth()
        ) {
            AppTickerText(
                value = price,
                currencySymbol = currencySymbol,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )

            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(trendBg, RoundedCornerShape(30.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                AppTickerText(
                    value = percentChange,
                    showExplicitSign = true,
                    suffix = "%",
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = trendColor
                    )
                )
            }
        }

        // --- Action Section: Delete ---
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete $symbol", // Added for accessibility
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}