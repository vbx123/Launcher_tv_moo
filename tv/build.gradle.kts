plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nasim.launcher.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nasim.launcher.tv"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // توقيع بمفتاح debug عشان الـ APK يتثبت مباشرة بدون keystore
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
