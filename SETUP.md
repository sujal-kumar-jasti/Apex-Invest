# Installation & Configuration Guide

Follow these steps to configure the environment, set up the backend connection, and build the **Apex Invest** application.

## 1. Configure Firebase

The repository currently includes a placeholder or existing `google-services.json` file.

**Important Security Warning:**
You **must** replace `app/google-services.json` with your own configuration file downloaded from the Firebase Console.

It is **highly recommended** to add `app/google-services.json` to your `.gitignore` file to prevent leaking your private project credentials in the future.

## 2. Backend Setup (Hugging Face)

The app relies on a Python backend hosted on **Hugging Face Spaces**.

1.  **Verify Status:** Ensure your Hugging Face Space is active and running. If the Space is "Paused" or "Building," the app will fail to fetch stock data and predictions.
2.  **Update API Endpoints:**
    - Open the `api/` package in the Android project.
    - Locate the base URL constants (e.g., `BASE_URL`).
    - Update them to point to your specific Hugging Face Space URL (e.g., `https://sujal-apex-invest.hf.space`).

## 3. Build and Run

### Prerequisites
- Android Studio
- Java JDK 11+
- Android SDK

### Build Steps
1.  Open the project in **Android Studio**.
2.  Sync Gradle files to ensure dependencies are loaded.
3.  Run the application using the command line:

```bash
./gradlew assembleDebug
```
4.  Install on a connected device or emulator:
```bash
./gradlew installDebug
```
