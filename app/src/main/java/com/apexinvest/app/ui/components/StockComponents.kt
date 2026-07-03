package com.apexinvest.app.ui.components

<<<<<<< HEAD
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
=======
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
import androidx.compose.ui.unit.sp
=======
import androidx.compose.ui.zIndex
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
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

<<<<<<< HEAD
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; active = true },
            placeholder = { Text("Search Symbols (e.g. NVDA)", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        viewModel.clearSearchResults()
                        active = false
                    }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        if (active && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(searchResults) { result ->
                        ListItem(
                            headlineContent = { Text(result.symbol, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
                            supportingContent = { Text(result.name, style = MaterialTheme.typography.labelMedium, color = Color.Gray) },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(result.symbol.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            },
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                            modifier = Modifier.clickable {
                                query = result.symbol
                                active = false
                                viewModel.clearSearchResults()
                                onStockSelected(result.symbol)
                            }
                        )
<<<<<<< HEAD
=======
                        HorizontalDivider()
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                    }
                }
            }
        }
    }
<<<<<<< HEAD
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}