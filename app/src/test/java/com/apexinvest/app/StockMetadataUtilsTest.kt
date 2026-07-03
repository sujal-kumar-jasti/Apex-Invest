package com.apexinvest.app

import com.apexinvest.app.util.StockMetadataUtils
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.guessCurrencyFromSymbol
import org.junit.Assert.assertEquals
import org.junit.Test

class StockMetadataUtilsTest {

    @Test
    fun testExchangeDecoding() {
        assertEquals("NSE India", StockMetadataUtils.getExchangeInfo("RELIANCE.NS").name)
        assertEquals("INR", StockMetadataUtils.getExchangeInfo("RELIANCE.NS").currency)
        assertEquals("NSE", StockMetadataUtils.getExchangeInfo("RELIANCE.NS").tvPrefix)
        assertEquals("Asia/Kolkata", StockMetadataUtils.getExchangeInfo("RELIANCE.NS").timezone)

        assertEquals("US Exchanges (NYSE/NASDAQ)", StockMetadataUtils.getExchangeInfo("AAPL").name)
        assertEquals("USD", StockMetadataUtils.getExchangeInfo("AAPL").currency)
        assertEquals("NASDAQ", StockMetadataUtils.getExchangeInfo("AAPL").tvPrefix)
        assertEquals("America/New_York", StockMetadataUtils.getExchangeInfo("AAPL").timezone)
    }

    @Test
    fun testMarketOpenStatus() {
        // This test depends on the current time, so we just check it doesn't crash
        val (isOpen, status) = StockMetadataUtils.isMarketOpen("AAPL")
        println("AAPL Market Open: $isOpen, Status: $status")
        
        val (isOpenIn, statusIn) = StockMetadataUtils.isMarketOpen("RELIANCE.NS")
        println("RELIANCE Market Open: $isOpenIn, Status: $statusIn")
    }

    @Test
    fun testTradingViewSymbol() {
        assertEquals("RELIANCE.NS:NSE", StockMetadataUtils.getTradingViewSymbol("RELIANCE.NS"))
        assertEquals("AAPL:NASDAQ", StockMetadataUtils.getTradingViewSymbol("AAPL"))
        assertEquals("BAZA3.SA:BMFBOVESPA", StockMetadataUtils.getTradingViewSymbol("BAZA3.SA"))
        assertEquals("1322.T:TSE", StockMetadataUtils.getTradingViewSymbol("1322.T"))
    }

    @Test
    fun testCurrencyConversion() {
        val rates = mapOf("INR" to 84.0, "EUR" to 0.9, "USD" to 1.0, "JPY" to 150.0, "CAD" to 1.35)
        
        // 100 EUR to USD: 100 / 0.9 = 111.11
        val resultEurToUsd = getConvertedValue(100.0, "SAP.DE", true, rates)
        assertEquals(111.11, resultEurToUsd, 0.01)

        // 100 EUR to INR: 111.11 * 84 = 9333.33
        val resultEurToInr = getConvertedValue(100.0, "SAP.DE", false, rates)
        assertEquals(9333.33, resultEurToInr, 0.01)

        // 15000 JPY to USD: 15000 / 150 = 100.0
        val resultJpyToUsd = getConvertedValue(15000.0, "7203.T", true, rates)
        assertEquals(100.0, resultJpyToUsd, 0.01)

        // 100 CAD to USD: 100 / 1.35 = 74.07
        val resultCadToUsd = getConvertedValue(100.0, "SHOP.TO", true, rates)
        assertEquals(74.07, resultCadToUsd, 0.01)
    }

    @Test
    fun testGuessCurrency() {
        assertEquals("JPY", guessCurrencyFromSymbol("7203.T"))
        assertEquals("EUR", guessCurrencyFromSymbol("SAP.DE"))
        assertEquals("CAD", guessCurrencyFromSymbol("SHOP.TO"))
        assertEquals("INR", guessCurrencyFromSymbol("RELIANCE.NS"))
        assertEquals("USD", guessCurrencyFromSymbol("AAPL"))
        
        // Indices
        assertEquals("JPY", guessCurrencyFromSymbol("^N225"))
        assertEquals("HKD", guessCurrencyFromSymbol("^HSI"))
        assertEquals("INR", guessCurrencyFromSymbol("NIFTY 50"))
    }
}
