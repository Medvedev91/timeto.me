buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        // WARNING Sync version with shared build.gradle.kts
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.4")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
