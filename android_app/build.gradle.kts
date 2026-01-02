plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

android {

    namespace = "me.timeto.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.timeto.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 596
        versionName = "2026.01.01"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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

    // https://gist.github.com/obfusk/61046e09cee352ae6dd109911534b12e#fix-proposed-by-linsui-disable-baseline-profiles
    tasks.whenTaskAdded {
        if (name.contains("ArtProfile")) {
            enabled = false
        }
    }

    // https://f-droid.org/en/docs/Reproducible_Builds/#png-crushcrunch
    packaging.resources { aaptOptions.cruncherEnabled = false }

    compileOptions.sourceCompatibility = JavaVersion.VERSION_21
    compileOptions.targetCompatibility = JavaVersion.VERSION_21

    buildFeatures.buildConfig = true
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.compose.material:material:1.10.0")
    implementation("com.google.android.material:material:1.13.0")
}
