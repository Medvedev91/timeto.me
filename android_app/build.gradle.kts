plugins {
    kotlin("android")
    id("com.android.application")
}

android {

    namespace = "me.timeto.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "me.timeto.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 461
        versionName = "2024.01.16"
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        // Also used for "isFDroid" variable
        create("fdroid") {
            initWith(getByName("release"))
        }
    }

    // https://f-droid.org/en/docs/Reproducible_Builds/#png-crushcrunch
    packaging.resources { aaptOptions.cruncherEnabled = false }

    compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    compileOptions.targetCompatibility = JavaVersion.VERSION_17

    buildFeatures.compose = true
    buildFeatures.buildConfig = true

    composeOptions.kotlinCompilerExtensionVersion = "1.5.6"
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("com.google.android.material:material:1.10.0")
}
