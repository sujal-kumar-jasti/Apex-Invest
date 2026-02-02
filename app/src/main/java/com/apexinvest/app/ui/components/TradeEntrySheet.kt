package com.apexinvest.app.ui.components

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.apexinvest.app.data.StockSearchResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeEntrySheet(
    portfolioStocks: List<String>,
    searchResults: List<StockSearchResult>,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean, String, String, String) -> Unit,
    initialQuery: String = ""
) {
    // --- State ---
    var symbol by remember { mutableStateOf(initialQuery) }

    // KEY FIX: Even if text is pre-filled, keep validSelection NULL so the list stays open/active.
    var validSelection by remember { mutableStateOf<String?>(null) }

    // KEY FIX: Force dropdown to be TRUE initially if there is text.
    var showDropdown by remember { mutableStateOf(initialQuery.isNotEmpty()) }

    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isBuy by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }

    val primaryColor by animateColorAsState(
        targetValue = if (isBuy) Color(0xFF00C853) else Color(0xFFD32F2F),
        label = "ButtonColor"
    )

    val currencySymbol = if (validSelection?.uppercase()?.endsWith(".NS") == true || validSelection?.uppercase()?.endsWith(".BO") == true) "₹" else "$"

    // Trigger initial search immediately so list populates instantly
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            onSearch(initialQuery)
            showDropdown = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBuy) "Buy Stock" else "Sell Stock",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = Color.Gray)
            }
        }

        Spacer(Modifier.height(24.dp))

        // 2. Buy/Sell Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            val buyAlpha by animateFloatAsState(if (isBuy) 1f else 0f, label = "buyAlpha")
            val sellAlpha by animateFloatAsState(if (!isBuy) 1f else 0f, label = "sellAlpha")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00C853).copy(alpha = buyAlpha))
                    .clickable {
                        isBuy = true
                        showDropdown = false
                        validSelection = null
                        symbol = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("BUY", fontWeight = FontWeight.Bold, color = if (isBuy) Color.White else Color.Gray)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD32F2F).copy(alpha = sellAlpha))
                    .clickable {
                        isBuy = false
                        showDropdown = false
                        validSelection = null
                        symbol = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("SELL", fontWeight = FontWeight.Bold, color = if (!isBuy) Color.White else Color.Gray)
            }
        }

        Spacer(Modifier.height(24.dp))

        // 3. Search Field
        Box(modifier = Modifier.zIndex(2f)) {
            OutlinedTextField(
                value = symbol,
                onValueChange = {
                    symbol = it.uppercase()
                    validSelection = null // Ensure list stays active on edit
                    showDropdown = true
                    if (isBuy) onSearch(it)
                },
                label = { Text(if (isBuy) "Search Stock" else "Portfolio Stock") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = if (validSelection != null) primaryColor else MaterialTheme.colorScheme.outline
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                singleLine = true,
                trailingIcon = {
                    if (validSelection != null) {
                        IconButton(onClick = {
                            symbol = ""
                            validSelection = null
                            showDropdown = false
                        }) {
                            Icon(Icons.Default.CheckCircle, "Valid", tint = primaryColor)
                        }
                    } else {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            )

            // Suggestions List
            // LOGIC FIX: Show list if dropdown is active AND we haven't locked a selection yet
            if (showDropdown && symbol.isNotEmpty() && validSelection == null) {
                val suggestions = if (isBuy) {
                    searchResults
                } else {
                    portfolioStocks.filter { it.contains(symbol, ignoreCase = true) }
                }

                if (suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                            .heightIn(max = 220.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        LazyColumn {
                            items(suggestions) { item ->
                                val stockSym = if (item is StockSearchResult) item.symbol else item.toString()
                                val stockName = if (item is StockSearchResult) item.shortName ?: "" else ""

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            symbol = stockSym
                                            validSelection = stockSym
                                            showDropdown = false // Hide list only after clicking item
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(stockSym, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        if (stockName.isNotEmpty()) {
                                            Text(stockName, color = Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                            }
                        }
                    }
                } else if (isBuy && symbol.length > 2) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text("No stocks found.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 4. Quantity & Price
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.all { c -> c.isDigit() }) quantity = it },
                label = { Text("Qty") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // 5. Date Picker
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
            enabled = false,
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = primaryColor) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = primaryColor,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(Modifier.height(32.dp))

        // 6. Submit Button
        val isFormValid = validSelection != null && quantity.isNotBlank() && price.isNotBlank()

        Button(
            onClick = {
                if (isFormValid) {
                    onConfirm(validSelection!!, isBuy, quantity, price, selectedDate)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
            ),
            enabled = isFormValid
        ) {
            Text(
                text = if (!isFormValid) "Select Stock First" else if (isBuy) "ADD TO PORTFOLIO" else "RECORD SALE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}