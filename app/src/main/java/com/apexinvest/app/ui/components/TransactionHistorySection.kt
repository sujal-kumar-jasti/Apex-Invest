package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistorySection(
    history: List<TransactionEntity>,
    currencySymbol: String = "₹" // Default to Rupee, but parent can override
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Order History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (history.isEmpty()) {
            Text(
                text = "No transactions recorded yet.",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Display newest transactions first
            history.sortedByDescending { it.timestamp }.forEach { transaction ->
                TransactionRow(transaction, currencySymbol)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: TransactionEntity, currencySymbol: String) {
    val isBuy = transaction.type == TransactionType.BUY
    val color = if (isBuy) Color(0xFF00C853) else Color(0xFFD32F2F)
    val icon = if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward // Down = In (Buy), Up = Out (Sell)

    val formattedDate = try {
        // Format timestamp to readable date (e.g., "Oct 24, 2025")
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(transaction.timestamp))
    } catch (e: Exception) {
        "-"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Icon Bubble
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // 2. Type & Date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isBuy) "BUY ORDER" else "SELL ORDER",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // 3. Price & Quantity
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$currencySymbol${transaction.price}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${transaction.quantity} shares",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}