package me.timeto.shared

import io.ktor.client.request.HttpRequestBuilder

fun HttpRequestBuilder.urlAppendSystemInfo(
    token: String?,
) {
    url {
        val systemInfo = SystemInfo.instance
        parameters.append("__token", token ?: "")
        parameters.append("__build", systemInfo.build.toString())
        parameters.append("__os", systemInfo.os.fullVersion)
        parameters.append("__device", systemInfo.device)
        parameters.append("__flavor", systemInfo.flavor ?: "")
    }
}
