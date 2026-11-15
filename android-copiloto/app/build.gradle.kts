plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.meuuber99pro.copiloto"
    compileSdk = 35
    flavorDimensions += "modo"
    productFlavors {
        create("demo") {
            dimension = "modo"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo-sem-acessibilidade"
        }
        create("automatico") {
            dimension = "modo"
            versionNameSuffix = "-experimental-acessibilidade"
        }
    }
    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "br.com.meuuber99pro.copiloto"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2-print-local"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Modelo LATIN incluído no APK: OCR local, sem baixar imagens ou textos.
    implementation("com.google.mlkit:text-recognition:16.0.1")
    testImplementation("junit:junit:4.13.2")
}
