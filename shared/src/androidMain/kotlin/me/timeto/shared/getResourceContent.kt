package me.timeto.shared

import java.io.InputStreamReader

actual fun getResourceContent(file: String, type: String) = androidApplication
    .resources
    .openRawResource(
        // No type, only file name without extension.
        androidApplication.resources.getIdentifier(file, "raw", androidApplication.packageName)
    )
    .use { stream ->
        InputStreamReader(stream).use { reader ->
            reader.readText()
        }
    }
