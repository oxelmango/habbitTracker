plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.magic.habbittracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.magic.habbittracker"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Enable app bundle optimization
        setProperty("archivesBaseName", "habbit-tracker-v$versionName")
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable resources optimization
        resourceConfigurations.addAll(listOf("en"))
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
    
    // Configure R8 full mode for better optimization
    buildFeatures {
        compose = true
    }
    
    // Optimize Compose compiler
    composeOptions {
        useLiveLiterals = true
    }
    
    // Split APKs by ABI
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
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
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose dependencies with BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    
    // For Compose navigation
    implementation(libs.androidx.navigation.compose)
    
    // WorkManager for notifications - needed for background work
    implementation(libs.androidx.work.runtime.ktx)
    
    // DataStore for preferences - needed for storing user data
    implementation(libs.androidx.datastore.preferences)
    
    // Kotlinx Serialization - needed for JSON import/export
    implementation(libs.kotlinx.serialization.json)
    

}