import java.util.Properties

// Leer local.properties de forma segura
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) load(localFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)  // Procesador de anotaciones para Room (migrado de kapt)
}

android {
    namespace = "com.toka.studyboost"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.toka.studyboost"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API Key segura — leída de local.properties, nunca hardcodeada
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties["GEMINI_API_KEY"] ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        compose = true
        buildConfig = true // Necesario para BuildConfig.GEMINI_API_KEY
    }

    sourceSets {
        getByName("main") {
            java.srcDir("build/generated/ksp/main/kotlin")
        }
        getByName("debug") {
            java.srcDir("build/generated/ksp/debug/kotlin")
        }
    }
}

dependencies {
    // —— Core Compose ——————————————————————————————————————————————————————————
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // —— Navigation ————————————————————————————————————————————————————————————
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // —— ViewModel —————————————————————————————————————————————————————————————
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // collectAsStateWithLifecycle() — más eficiente que collectAsState()
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // —— Material Icons Extended ————————————————————————————————————————————————
    implementation("androidx.compose.material:material-icons-extended")

    // —— DataStore (sesión de usuario) ——————————————————————————————————————————
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // —— JSON ——————————————————————————————————————————————————————————————————
    implementation("com.google.code.gson:gson:2.11.0")

    // —— PDF (extracción de texto) ——————————————————————————————————————————————
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    val roomVersion = "2.7.1"  // ← actualizado de 2.6.1 (fix: KSP jvm signature V)
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // —— ML Kit (OCR — cámara a texto) ——————————————————————————————————————————
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // —— Retrofit (cliente HTTP tipado) ————————————————————————————————————————
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // —— Tests —————————————————————————————————————————————————————————————————
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
