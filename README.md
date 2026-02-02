# Apex Invest

**Apex Invest** is a comprehensive stock market tracking and portfolio management application built with **Kotlin** and **Jetpack Compose**. It leverages **Generative AI (Gemini)** and **Sentiment Analysis** to provide actionable financial insights, real-time tracking, and market predictions.

The project is organized under the package `com.apexinvest.app` and follows modern Android architecture patterns (MVVM). The entire backend logic, including stock data processing and sentiment analysis models, is hosted on the **Hugging Face Cloud Platform**.

![Platform: Android](https://img.shields.io/badge/Platform-Android-green)
![Tech: Jetpack Compose](https://img.shields.io/badge/Built%20With-Jetpack%20Compose-blue)
![Backend: Hugging Face](https://img.shields.io/badge/Backend-Hugging%20Face%20Spaces-yellow)

## Key Features

### AI-Powered Insights
- **Gemini AI Integration:** Generates 3 actionable stock suggestions which can be directly converted into "Buy" trades within the portfolio.
- **Sentiment Prediction Engine:** A custom Python backend hosted on **Hugging Face** runs text classification on news from Google News. It assigns a **sentiment score (-1 to +1)** to your portfolio based on recent trends.
- **Portfolio Diagnosis:** AI analysis of current holdings to identify risks and opportunities.

### Advanced Analytics & Portfolio
- **Deep Portfolio Analysis:**
  - **Sector Distribution:** Visual bar charts showing exposure across industries.
  - **Asset Weightage:** Progress bars indicating the percentage of total capital per stock.
  - **1-Day Simulation:** Project potential portfolio performance based on daily volatility.
- **Transaction Management:** Add trades (Buy/Sell) with specific dates and prices, or link Demat accounts manually.
- **Background Updates:** Uses `WorkManager` for scheduled tasks and watchlist notifications (4-hour intervals).

### Market Explorer
- **Global & Local Tracking:** Specialized Python endpoints on Hugging Face fetch data for **Indian Markets** (Nifty 50, Bank Nifty) and **US Markets/Commodities**.
- **Top Gainers & News:** Dedicated section for top performing stocks and integrated Google News feeds.
- **Smart Watchlist:** Toggle currency (USD/INR) and enable price alerts.

## Technical Architecture

The application is configured with **Gradle Kotlin DSL** and is structured into clear layers for maintainability.

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Backend:** Firebase (Auth, Firestore/Realtime Database)
- **Cloud Infrastructure:** Hugging Face Spaces (Python Backend & ML Models)
- **ML/AI:** Google Gemini API, Hugging Face Transformers

### Project Structure
The source code is located in `app/src/main/java/com/apexinvest/app/`:

- **api/**: Network configurations and Retrofit service definitions connecting to Hugging Face endpoints.
- **data/**: Repositories and data models handling business logic.
- **db/**: Local database schemas and storage helpers.
- **ui/**: Jetpack Compose screens and UI components.
- **viewmodel/**: State management classes following the MVVM pattern.
- **worker/**: Background tasks (WorkManager) for periodic syncs and notifications.
- **util/**: Helper functions and extension methods.
- **schemas/**: JSON schemas and asset definitions.

## Author

**J Sujal Kumar**
- **GitHub:** [@sujal-kumar-jasti](https://github.com/sujal-kumar-jasti)
- **Email:** [sujalkumarjasti751@gmail.com](mailto:sujalkumarjasti751@gmail.com)

For installation and setup instructions, please refer to [SETUP.md](SETUP.md).
