package com.apexinvest.app.util

import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.viewmodel.PortfolioStats
import com.apexinvest.app.viewmodel.StockAllocation

object MathUtils {

    fun calculatePortfolioStats(
        portfolio: List<StockEntity>,
        isUsd: Boolean,
        rates: Map<String, Double>,
        sparklineCache: Map<String, List<Double>>
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
            
            // Calculate what the portfolio was worth at the start of the current daily session
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

        // 🛠️ FIX: Aggregate chart data with right-alignment (anchored to the latest point)
        val maxHistorySize = sparklineCache.values.maxOfOrNull { it.size } ?: 0
        val aggregatedChart = if (maxHistorySize > 0) {
            List(maxHistorySize) { index ->
                portfolio.sumOf { stock ->
                    val history = sparklineCache[stock.symbol] ?: emptyList()
                    // RIGHT-ALIGN: Calculate index relative to the end of each list
                    val offsetIndex = history.size - maxHistorySize + index
                    if (offsetIndex >= 0 && offsetIndex < history.size) {
                        getConvertedValue(history[offsetIndex] * stock.quantity, stock.symbol, isUsd, rates)
                    } else if (history.isNotEmpty()) {
                        // Use the earliest known price for points before the start of this specific stock's history
                        getConvertedValue(history.first() * stock.quantity, stock.symbol, isUsd, rates)
                    } else {
                        getConvertedValue(stock.currentPrice * stock.quantity, stock.symbol, isUsd, rates)
                    }
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
