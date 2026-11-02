plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nasim.launcher.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nasim.launcher.tv"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "2.0"
    }
}

dependencies {
    implementation(project(":common"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.zxing:core:3.5.2")
}
