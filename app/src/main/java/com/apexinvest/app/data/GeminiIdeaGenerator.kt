package com.apexinvest.app.data

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiIdeaGenerator(apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    // --- PORTFOLIO ANALYSIS (Rich Content + Strict Format) ---
    suspend fun generatePortfolioIdeas(portfolioSummary: String): String = withContext(Dispatchers.IO) {
        // Handle empty case gracefully
        if (portfolioSummary.contains("no holdings", ignoreCase = true)) {
            return@withContext """
                [OPPORTUNITY] Fresh Start: Your portfolio is currently empty. This is the perfect time to build a solid core foundation using low-cost Index ETFs.
                [RISK] Inflation Erosion: Holding strictly cash causes you to lose purchasing power over time. Consider deploying capital into stable assets.
                [SUGGESTION] NIFTYBEES.NS | ETF | Low-risk stability.
                [SUGGESTION] RELIANCE.NS | Energy | Market leader.
                [SUGGESTION] HDFCBANK.NS | Bank | Long-term growth.
            """.trimIndent()
        }

        val prompt = """
            You are an expert Hedge Fund Manager. Analyze this portfolio deeply:
            $portfolioSummary
            
            OUTPUT RULES:
            1. Use [RISK] for the biggest danger (e.g., Over-concentration, Volatility).
            2. Use [OPPORTUNITY] for the best move (e.g., Sector rotation, Undervalued picks).
            3. Suggest 3 specific stocks using [SUGGESTION].
            
            STRICT FORMATTING (You MUST use a colon ':' to separate Title and Description):
            
            [RISK] Short Title: Write a detailed 2-sentence explanation of the risk here.
            [OPPORTUNITY] Short Title: Write a detailed 2-sentence explanation of the opportunity here.
            [SUGGESTION] SYMBOL | Sector | Short reason
            
            Generate now:
        """.trimIndent()

        return@withContext safeGenerate(prompt)
    }

    // --- THEMATIC ANALYSIS (Rich Content + Strict Format) ---
    suspend fun generateThematicIdeas(theme: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            Theme: "$theme".
            
            OUTPUT RULES:
            1. [OPPORTUNITY]: Why is this theme trending now? (Give detail).
            2. [RISK]: What is the biggest threat to this sector? (Give detail).
            3. [SUGGESTION]: Pick 3 stocks that fit this theme best.
            
            STRICT FORMATTING (Use colon ':' separator):
            
            [OPPORTUNITY] Trend Alert: Write a detailed explanation of why this sector is growing.
            [RISK] Major Risk: Write a detailed explanation of the headwinds facing this sector.
            [SUGGESTION] SYMBOL | Sector | Short reason
            
            Generate now:
        """.trimIndent()

        return@withContext safeGenerate(prompt)
    }

    private suspend fun safeGenerate(prompt: String): String {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: fallbackResponse()
        } catch (e: Exception) {
            fallbackResponse()
        }
    }

    private fun fallbackResponse(): String {
        return """
            [RISK] Connection Failed: We couldn't reach the AI server. Please check your internet connection.
            [OPPORTUNITY] Market Volatility: During uncertain times, focus on blue-chip stocks and gold for stability.
            [SUGGESTION] NIFTYBEES.NS | ETF | Stability.
        """.trimIndent()
    }
}