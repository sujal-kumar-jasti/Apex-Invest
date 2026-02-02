Apex Invest

Apex Invest is a comprehensive stock market tracking and portfolio management application built with Kotlin and Jetpack Compose. It leverages Generative AI (Gemini) and Sentiment Analysis (Hugging Face) to provide actionable financial insights, real-time tracking, and market predictions.

The project is organized under the package com.apexinvest.app and follows modern Android architecture patterns (MVVM), utilizing dedicated microservices for data fetching and analysis.






Key Features
AI-Powered Insights

Gemini AI Integration: Generates 3 actionable stock suggestions which can be directly converted into Buy trades within the portfolio.

Sentiment Prediction Engine: A custom Python backend hosted on Hugging Face runs text classification on news from Google News. It assigns a sentiment score (-1 to +1) to the portfolio based on recent trends.

Portfolio Diagnosis: AI analysis of current holdings to identify risks and opportunities.

Advanced Analytics & Portfolio

Deep Portfolio Analysis

Sector Distribution: Visual bar charts showing exposure across industries.

Asset Weightage: Progress bars indicating the percentage of total capital per stock.

1-Day Simulation: Projects potential portfolio performance based on daily volatility.

Transaction Management: Add trades (Buy/Sell) with specific dates and prices, or link Demat accounts manually.

Background Updates: Uses WorkManager for scheduled tasks and watchlist notifications (4-hour intervals).

Market Explorer

Global & Local Tracking: Separate microservices fetch data for Indian Markets (Nifty 50, Bank Nifty) and US Markets/Commodities.

Top Gainers & News: Dedicated section for top-performing stocks and integrated Google News feeds.

Smart Watchlist: Toggle currency (USD/INR) and enable price alerts.

Technical Architecture

The application is configured with Gradle Kotlin DSL and is structured into clear layers for maintainability.

Tech Stack

Language: Kotlin

UI Framework: Jetpack Compose (Material Design 3)

Backend: Firebase (Authentication, Firestore / Realtime Database)

Microservices: Python (hosted externally) for scraping and ML inference

ML / AI: Google Gemini API, Hugging Face Transformers

Project Structure

The source code is located in app/src/main/java/com/apexinvest/app/:

api/ – Network configurations and Retrofit service definitions

data/ – Repositories and data models

db/ – Local database schemas and storage helpers

ui/ – Jetpack Compose screens and UI components

viewmodel/ – State management using MVVM

worker/ – Background tasks using WorkManager

util/ – Helper functions and extensions

schemas/ – JSON schemas and asset definitions

Prerequisites

Java JDK version 11 or later

Android Studio (recommended)

Firebase project for authentication and database features

Getting Started
Clone the repository

git clone https://github.com/sujal-kumar-jasti/Apex-Invest.git

cd Apex-Invest

Configure Firebase

The repository currently includes a placeholder or existing google-services.json file.

Important: For security, you must replace app/google-services.json with your own configuration file downloaded from the Firebase Console.

It is highly recommended to add app/google-services.json to your .gitignore file to prevent leaking your private project credentials in the future.

Backend Setup

Ensure the Python microservices are running.

Update the base URLs in the api/ package constants to point to your hosted instances or localhost.

Build and Run

Open the project in Android Studio and sync Gradle files.

Run the application:

./gradlew assembleDebug

Install on a connected device

./gradlew installDebug

Contributing

Contributions are welcome. Please follow these steps:

Fork the repository.

Create a feature branch (feature/your-feature).

Commit your changes.

Open a pull request.

Please ensure you adhere to the existing package structure (com.apexinvest.app) and Kotlin coding standards.

Author

J Sujal Kumar

Email: sujalkumarjasti751@gmail.com

GitHub: https://github.com/sujal-kumar-jasti
