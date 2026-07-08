package com.apexinvest.app.ui.screens.stockdetail.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.apexinvest.app.api.models.SimilarStock
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.GlassBorderDark
import com.apexinvest.app.ui.theme.GlassBorderLight
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol

@Composable
fun NewsAndPeersPane(news: List<StockNews>, peers: List<SimilarStock>, currency: String, isDark: Boolean, onPeerClick: (String) -> Unit) {
    val appColors = LocalAppColors.current
    if (peers.isNotEmpty()) {
        GlassPaneCard("Similar Assets", isDark) {
            peers.forEach { peer ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onPeerClick(peer.symbol) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(peer.symbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(peer.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${getCurrencySymbol(currency)}${peer.price.fmt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${if(peer.changePercent > 0) "+" else ""}${peer.changePercent.fmt()}%", color = if(peer.changePercent >= 0) appColors.trendGreen else appColors.trendRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = if(isDark) GlassBorderDark else GlassBorderLight)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    if (news.isNotEmpty()) {
        Text("Latest Headlines", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))
        val context = LocalContext.current
        news.forEach { article ->
            Column(Modifier.fillMaxWidth().glassCard(isDark, RoundedCornerShape(16.dp)).clickable { article.link?.let { try { context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri())) } catch (_: Exception) {} } }.padding(16.dp)) {
                Text(article.publisher ?: "Market News", fontSize = 11.sp, color = BrandPurple, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(article.title ?: "No Title", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(article.published ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
