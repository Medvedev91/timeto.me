plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("app.cash.sqldelight").version("2.1.0")
}

kotlin {

    androidTarget()

    listOf(
        // iOS
        iosArm64(),
        iosSimulatorArm64(),
        // WatchOS
        watchosX64(),
        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {

        val ktor_version = "2.3.11"
        val sqldelight_version = "2.1.0"

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                implementation("app.cash.sqldelight:primitive-adapters:$sqldelight_version")
                implementation("app.cash.sqldelight:coroutines-extensions:$sqldelight_version")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktor_version")
                implementation("app.cash.sqldelight:android-driver:$sqldelight_version")
            }
        }

        val appleMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor_version")
                implementation("app.cash.sqldelight:native-driver:$sqldelight_version")
            }
        }

        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            dependsOn(appleMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val watchosX64Main by getting
        val watchosArm32Main by getting
        val watchosArm64Main by getting
        val watchosSimulatorArm64Main by getting
        val watchosMain by creating {
            dependsOn(commonMain)
            dependsOn(appleMain)
            watchosX64Main.dependsOn(this)
            watchosArm32Main.dependsOn(this)
            watchosArm64Main.dependsOn(this)
            watchosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    namespace = "me.timeto.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    compileOptions.targetCompatibility = JavaVersion.VERSION_17
}

sqldelight {
    databases {
        create("TimetomeDB") {
            packageName.set("me.timeto.appdbsq")
        }
    }
}
