package me.timeto.shared

internal data class SystemInfo(
    val build: Int,
    val version: String,
    val os: Os,
    val device: String,
    val flavor: String?,
) {

    val isFdroid: Boolean = (flavor == "fdroid")

    ///

    sealed class Os(
        val version: String,
    ) {

        val fullVersion: String = run {
            val prefix: String = when (this) {
                is Android -> "android"
                is Ios -> "ios"
                is Watchos -> "watchos"
            }
            "$prefix-$version"
        }

        ///

        class Android(version: String) : Os(version)
        class Ios(version: String) : Os(version)
        class Watchos(version: String) : Os(version)
    }
}
