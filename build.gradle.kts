buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.11.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
