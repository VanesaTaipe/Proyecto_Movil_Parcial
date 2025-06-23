import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.proyecto_movil_parcial"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyecto_movil_parcial"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API KEY segura
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }

        val openaiApiKey = localProperties.getProperty("OPENAI_API_KEY") ?: "YOUR_API_KEY_HERE"
        buildConfigField("String", "OPENAI_API_KEY", "\"$openaiApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
         resources {
             excludes += "META-INF/LICENSE.md" // Nota la ausencia de la barra inicial
             excludes += "META-INF/LICENSE-notice.md"
             excludes += "META-INF/LICENSE"
             excludes += "META-INF/NOTICE.md"
             excludes += "META-INF/NOTICE"
             excludes += "META-INF/DEPENDENCIES"
             excludes += "META-INF/AL2.0"
             excludes += "META-INF/LGPL2.1"
         }
    }
}

dependencies {

    // --- Core de Android y Jetpack ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2") // Para StateFlow y viewModelScope
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose UI ---
    implementation(platform(libs.androidx.compose.bom)) // BOM para Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // --- Firebase y autenticación ---
    implementation(platform("com.google.firebase:firebase-bom:33.13.0")) // BOM de Firebase
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation(libs.firebase.auth)

    // --- Networking (Retrofit + Gson) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- Corrutinas ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- Pruebas unitarias ---
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0") // Para testear flows
    testImplementation("io.mockk:mockk:1.13.10") // (recomendado agregar si haces tests unitarios puros con mockk)

    // --- Pruebas instrumentadas (Android) ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("io.mockk:mockk-android:1.13") // Mocking en AndroidTest

    // --- Debug y herramientas de testing UI ---
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
