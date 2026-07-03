package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.apexinvest.app.api.models.StockSearchResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeEntrySheet(
    onDismiss: () -> Unit,
    portfolioStocks: List<String>,
    searchResults: List<StockSearchResult>,
    initialQuery: String,
    onSearch: (String) -> Unit,
    onConfirm: (symbol: String, isBuy: Boolean, qty: String, price: String, date: String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val focusManager = LocalFocusManager.current

    val quantityFocus = remember { FocusRequester() }
    val priceFocus = remember { FocusRequester() }

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedSymbol by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var isBuy by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()) }
    val selectedDateStr = remember(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
        formatter.format(Instant.ofEpochMilli(millis))
    }

    val buyColor = if (isDark) Color(0xFF00E676) else Color(0xFF00C853)
    val sellColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)
    val activeColor = if (isBuy) buyColor else sellColor
    val inputBgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            if (isBuy) onSearch(initialQuery)
            selectedSymbol = initialQuery
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    isDropdownExpanded = false
                })
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Record Trade", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().height(48.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), RoundedCornerShape(12.dp)).padding(4.dp)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(if (isBuy) buyColor else Color.Transparent).clickable {
                isBuy = true
                if (searchQuery.isNotEmpty()) onSearch(searchQuery)
            }, contentAlignment = Alignment.Center) {
                Text("BUY", fontWeight = FontWeight.Black, color = if (isBuy) Color.White else Color.Gray, fontSize = 14.sp)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(if (!isBuy) sellColor else Color.Transparent).clickable {
                isBuy = false
            }, contentAlignment = Alignment.Center) {
                Text("SELL", fontWeight = FontWeight.Black, color = if (!isBuy) Color.White else Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InputLabel("ASSET")

        Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it.uppercase()
                    selectedSymbol = ""
                    isDropdownExpanded = true
                    if (isBuy) onSearch(it)
                },
                placeholder = { Text(if (isBuy) "Search Symbol" else "Search Portfolio") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused && searchQuery.isNotEmpty()) isDropdownExpanded = true },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    focusedBorderColor = if (selectedSymbol.isNotEmpty()) activeColor else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = borderColor
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                singleLine = true
            )

            val displayList = if (isBuy) {
                searchResults.map { it.symbol }.take(5)
            } else {
                portfolioStocks.filter { it.contains(searchQuery, ignoreCase = true) }.take(5)
            }

            val showList = isDropdownExpanded && displayList.isNotEmpty() && searchQuery.isNotEmpty()

            // Removed Animation Visibility wrapper for instant results
            if (showList) {
                Surface(
                    modifier = Modifier
                        .padding(top = 60.dp)
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        displayList.forEach { symbol ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = symbol
                                        selectedSymbol = symbol
                                        isDropdownExpanded = false
                                        quantityFocus.requestFocus()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(symbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(8.dp))
                                if (portfolioStocks.contains(symbol)) {
                                    Surface(color = MaterialTheme.colorScheme.primary.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                                        Text("Owned", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.2f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val currencySymbol = com.apexinvest.app.util.getCurrencySymbol(com.apexinvest.app.util.guessCurrencyFromSymbol(selectedSymbol))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                InputLabel("QUANTITY")
                OutlinedTextField(
                    value = quantity,
                    // Regex allows decimals: e.g., "10.5"
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it },
                    placeholder = { Text("0") },
                    enabled = selectedSymbol.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().focusRequester(quantityFocus),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor,
                        disabledContainerColor = inputBgColor.copy(0.1f),
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = borderColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { priceFocus.requestFocus() }),
                    singleLine = true
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                InputLabel("PRICE")
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                    placeholder = { Text("0.00") },
                    enabled = selectedSymbol.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().focusRequester(priceFocus),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBgColor,
                        unfocusedContainerColor = inputBgColor,
                        disabledContainerColor = inputBgColor.copy(0.1f),
                        focusedBorderColor = activeColor,
                        unfocusedBorderColor = borderColor
                    ),
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InputLabel("DATE")
        val dateSource = remember { MutableInteractionSource() }
        LaunchedEffect(dateSource) {
            dateSource.interactions.collect { if (it is PressInteraction.Release) showDatePicker = true }
        }
        OutlinedTextField(
            value = selectedDateStr, onValueChange = {}, readOnly = true, interactionSource = dateSource,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = inputBgColor, unfocusedContainerColor = inputBgColor, focusedBorderColor = activeColor, unfocusedBorderColor = borderColor),
            trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = activeColor) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        val isFormValid = selectedSymbol.isNotEmpty() && quantity.isNotEmpty() && price.isNotEmpty()

        Button(
            onClick = {
                if (isFormValid) {
                    focusManager.clearFocus()
                    // Sends raw quantity string to ensure decimals are preserved
                    onConfirm(selectedSymbol, isBuy, quantity, price, selectedDateStr)
                }
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = activeColor,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isFormValid) 4.dp else 0.dp)
        ) {
            Text(
                text = if (selectedSymbol.isEmpty() && searchQuery.isNotEmpty()) "Select Asset from List" else "Execute ${if (isBuy) "Buy" else "Sell"}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}