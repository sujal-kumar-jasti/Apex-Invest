package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
<<<<<<< HEAD
import androidx.compose.foundation.layout.Arrangement
=======
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
<<<<<<< HEAD
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
=======
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
import androidx.compose.ui.unit.sp
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.TransactionType
import com.apexinvest.app.util.toCleanString
=======
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.TransactionType
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistorySection(
    history: List<TransactionEntity>,
<<<<<<< HEAD
    currencySymbol: String = "₹"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- PRO HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Order Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${history.size} Entries",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded market activity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            // Display newest transactions first
            history.sortedByDescending { it.timestamp }.forEachIndexed { index, transaction ->
                ProTransactionRow(transaction, currencySymbol)

                // Show divider between items, but not after the last one
                if (index < history.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }
}

@Composable
<<<<<<< HEAD
fun ProTransactionRow(transaction: TransactionEntity, currencySymbol: String) {
    val isBuy = transaction.type == TransactionType.BUY
    val trendColor = if (isBuy) Color(0xFF00E676) else Color(0xFFFF5252)
    val icon = if (isBuy) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    val formattedDate = try {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(transaction.timestamp))
    } catch (e: Exception) {
        "--/--/--"
=======
fun TransactionRow(transaction: TransactionEntity, currencySymbol: String) {
    val isBuy = transaction.type == TransactionType.BUY
    val color = if (isBuy) Color(0xFF00C853) else Color(0xFFD32F2F)
    val icon = if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward // Down = In (Buy), Up = Out (Sell)

    val formattedDate = try {
        // Format timestamp to readable date (e.g., "Oct 24, 2025")
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(transaction.timestamp))
    } catch (e: Exception) {
        "-"
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
<<<<<<< HEAD
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. STATUS INDICATOR (GLASS BUBBLE) ---
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(trendColor.copy(alpha = 0.1f)),
=======
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Icon Bubble
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
<<<<<<< HEAD
                tint = trendColor,
                modifier = Modifier.size(22.dp)
=======
                tint = color,
                modifier = Modifier.size(20.dp)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            )
        }

        Spacer(Modifier.width(16.dp))

<<<<<<< HEAD
        // --- 2. ORDER DETAILS (CENTER SYMMETRY) ---
        Column(modifier = Modifier.weight(1.1f)) {
            Text(
                text = if (isBuy) "Purchase" else "Disposal",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
=======
        // 2. Type & Date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isBuy) "BUY ORDER" else "SELL ORDER",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formattedDate,
<<<<<<< HEAD
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // --- 3. QUANTITY & VALUE (RIGHT SYMMETRY) ---
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$currencySymbol${transaction.price.toCleanString()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                color = trendColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${transaction.quantity} Units",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor,
                    fontWeight = FontWeight.Black
                )
            }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        }
    }
}