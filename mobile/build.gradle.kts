plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nasim.launcher.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nasim.launcher.mobile"
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
    implementation("com.google.android.material:material:1.11.0")
}
