import java.io.FileInputStream
import java.util.Properties

<<<<<<< HEAD
=======
// --------------------
// Load local.properties (Gemini API Key)
// --------------------
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

<<<<<<< HEAD
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

=======
val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""

// --------------------
// Plugins
// --------------------
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
kotlin {
    jvmToolchain(21)
}

<<<<<<< HEAD
android {
    namespace = "com.apexinvest.app"
    compileSdk = 37
=======
// --------------------
// Android Configuration
// --------------------
android {
    namespace = "com.apexinvest.app"
    compileSdk = 36
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

    defaultConfig {
        applicationId = "com.apexinvest.app"
        minSdk = 29
<<<<<<< HEAD
        targetSdk = 37
=======
        targetSdk = 36
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
<<<<<<< HEAD
    }

=======

        // Gemini API key
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"$geminiApiKey\""
        )
    }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

<<<<<<< HEAD
=======
// --------------------
// Global Dependency Fix
// --------------------
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}

<<<<<<< HEAD
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.androidx.compose.bom.v20260101))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.googleid)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugRuntimeOnly(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    runtimeOnly(libs.kotlinx.coroutines.android)
    implementation(libs.tink.android)
    
    // Explicitly keeping important core libraries that might be used directly
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20260101))
}

=======
// --------------------
// Dependencies
// --------------------
dependencies {

    implementation(libs.androidx.material3)
    // Versions
    val roomVersion = "2.8.4"
    val lifecycleVersion = "2.7.0"
    val retrofitVersion = "2.9.0"

    // --------------------
    // Core Android
    // --------------------
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.browser:browser:1.8.0")

    // --------------------
    // Compose
    // --------------------
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --------------------
    // Navigation & Lifecycle
    // --------------------
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // --------------------
    // Room (KSP)
    // --------------------
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // --------------------
    // Firebase
    // --------------------
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // --------------------
    // Networking
    // --------------------
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // --------------------
    // Coroutines & Work
    // --------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // --------------------
    // Images
    // --------------------
    implementation("io.coil-kt:coil-compose:2.4.0")

    // --------------------
    // Google Sign-In
    // --------------------
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // --------------------
    // Gemini AI
    // --------------------
    implementation("com.google.ai.client.generativeai:generativeai:0.1.0")

    // --------------------
    // Testing
    // --------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

// --------------------
// KSP Configuration
// --------------------
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}