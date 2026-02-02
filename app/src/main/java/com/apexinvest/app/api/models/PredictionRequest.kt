package com.apexinvest.app.api.models

// --- Request Model (What the app sends to FastAPI) ---
data class PredictionRequest(
    val symbol: String,
    val duration_days: Int
)

// --- Response Models (What the app receives from FastAPI) ---

// Represents a single forecasted price point
data class PredictionPoint(
    val date: String, // ISO format date string
    val predicted_price: Double
)

// Main Prediction Response
data class PredictionResponse(
    val symbol: String,
    val current_price: Double,
    val predictions: List<PredictionPoint>
)