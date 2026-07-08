package com.apexinvest.app.util

import com.apexinvest.app.api.models.CandlePointDto
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.viewmodel.PortfolioStats
import com.apexinvest.app.viewmodel.StockAllocation

object MathUtils {

    fun calculatePortfolioStats(
        portfolio: List<StockEntity>,
        isUsd: Boolean,
        rates: Map<String, Double>,
        sparklineCache: Map<String, List<CandlePointDto>>
    ): PortfolioStats {
        if (portfolio.isEmpty()) {
            return PortfolioStats(0.0, 0.0, 0.0, 0.0, true, emptyList(), 0.0, 0.0)
        }

        var totalValue = 0.0
        var totalInvested = 0.0
        var totalPrevValue = 0.0

        portfolio.forEach { stock ->
            val currentVal = getConvertedValue(stock.currentPrice * stock.quantity, stock.symbol, isUsd, rates)
            val investedVal = getConvertedValue(stock.buyPrice * stock.quantity, stock.symbol, isUsd, rates)
            
            // Calculate portfolio value at start of daily session
            val prevClosePrice = if (stock.previousClose > 0.0) stock.previousClose else stock.currentPrice
            val prevVal = getConvertedValue(prevClosePrice * stock.quantity, stock.symbol, isUsd, rates)

            totalValue += currentVal
            totalInvested += investedVal
            totalPrevValue += prevVal
        }

        val gain = totalValue - totalInvested
        val percent = if (totalInvested > 0) (gain / totalInvested) * 100 else 0.0
        
        val dailyGain = totalValue - totalPrevValue
        val dailyPercent = if (totalPrevValue > 0) (dailyGain / totalPrevValue) * 100 else 0.0

        // Aggregate chart data
        val rawMaxPoints = sparklineCache.values.maxOfOrNull { it.size } ?: 0
        // Ensure enough points for chart rendering
        val maxPoints =
            if (portfolio.isNotEmpty()) maxOf(60, minOf(rawMaxPoints, 250)) else 0
        
        val aggregatedChart = if (maxPoints > 0) {
            List(maxPoints) { i ->
                portfolio.sumOf { stock ->
                    val history = sparklineCache[stock.symbol] ?: emptyList()
                    // Right-align history index
                    val offset = history.size - maxPoints
                    val historyIndex = i + offset
                    
                    val price = when {
                        history.isEmpty() -> stock.currentPrice
                        historyIndex >= history.size -> history.last().close
                        historyIndex < 0 -> {
                            // Fallback to oldest history or previous close
                            history.firstOrNull()?.close ?: (if (stock.previousClose > 0) stock.previousClose else stock.currentPrice)
                        }
                        else -> history[historyIndex].close
                    }
                    getConvertedValue(price * stock.quantity, stock.symbol, isUsd, rates)
                }
            }
        } else emptyList()

        return PortfolioStats(
            totalValue = totalValue,
            totalInvested = totalInvested,
            totalGain = gain,
            totalPercent = percent,
            isPositive = gain >= 0,
            chartData = aggregatedChart,
            dailyGain = dailyGain,
            dailyPercent = dailyPercent
        )
    }

    fun calculateAllocations(
        portfolio: List<StockEntity>,
        totalValue: Double,
        isUsd: Boolean,
        rates: Map<String, Double>,
        getSector: (String) -> String
    ): Pair<List<StockAllocation>, Map<String, Double>> {
        val sectorMap = mutableMapOf<String, Double>()
        val allocations = portfolio.map { stock ->
            val sector = getSector(stock.symbol)
            val currentVal = getConvertedValue(stock.currentPrice * stock.quantity, stock.symbol, isUsd, rates)
            
            sectorMap[sector] = (sectorMap[sector] ?: 0.0) + currentVal

            StockAllocation(
                symbol = stock.symbol,
                value = currentVal,
                percent = if (totalValue > 0) currentVal / totalValue else 0.0,
                isProfit = stock.dailyChange >= 0,
                changePercent = stock.changePercent
            )
        }.sortedByDescending { it.value }
        
        return Pair(allocations, sectorMap)
    }
}
