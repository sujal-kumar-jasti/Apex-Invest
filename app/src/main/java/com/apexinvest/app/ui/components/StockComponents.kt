package com.apexinvest.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchField(
    viewModel: PortfolioViewModel,
    onStockSelected: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(query) {
        if (query.length > 1) viewModel.searchStocks(query) else viewModel.clearSearchResults()
    }

    Column(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; active = true },
            label = { Text("Search Stock") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = ""; viewModel.clearSearchResults(); active = false }) {
                    Icon(Icons.Default.Close, "Clear")
                }
            }
        )
        if (active && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                LazyColumn {
                    items(searchResults) { result ->
                        ListItem(
                            headlineContent = { Text(result.symbol, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${result.shortName ?: result.symbol}") },
                            modifier = Modifier.clickable {
                                query = result.symbol
                                active = false
                                viewModel.clearSearchResults()
                                onStockSelected(result.symbol)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun StockRow(stock: StockEntity, isUsd: Boolean, liveRate: Double, onClick: () -> Unit) {
    val currency = if (isUsd) "$" else "₹"
    val price = getConvertedValue(stock.currentPrice, stock.symbol, isUsd, liveRate)

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(stock.symbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("${stock.quantity} Shares", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$currency${price.toCleanString()}", fontWeight = FontWeight.Bold)
            Text(
                "${stock.dailyChange.toCleanString()}%",
                color = if (stock.dailyChange >= 0) Color(0xFF00C853) else Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
}