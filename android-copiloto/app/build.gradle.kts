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
        versionCode = 1
        versionName = "0.1-prototipo"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
