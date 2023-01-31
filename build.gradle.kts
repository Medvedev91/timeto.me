buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.5") // TRICK Sync with shared build.gradle.kts
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
