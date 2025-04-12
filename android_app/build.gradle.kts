plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

android {

    namespace = "me.timeto.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.timeto.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 572
        versionName = "2025.04.12"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "type"
    productFlavors {
        create("base") {
            dimension = "type"
        }
        create("fdroid") {
            dimension = "type"
        }
    }

    applicationVariants.all {
        outputs.all {
            this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            outputFileName = "$name.apk"
        }
    }

    // https://github.com/Medvedev91/timeto.me/issues/84
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    // https://f-droid.org/en/docs/Reproducible_Builds/#png-crushcrunch
    packaging.resources { aaptOptions.cruncherEnabled = false }

    compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    compileOptions.targetCompatibility = JavaVersion.VERSION_17

    buildFeatures.buildConfig = true
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material:material:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.android.material:material:1.12.0")
}
