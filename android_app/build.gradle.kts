plugins {
    kotlin("android")
    id("com.android.application")
}

android {

    namespace = "me.timeto.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "me.timeto.app"
        minSdk = 26
        targetSdk = 33
        versionCode = 324
        versionName = "2023.07.24"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = "1.4.0"
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.5.3")
}
