package io.github.kotlinmania.dotenvy

import java.util.concurrent.ConcurrentHashMap

private val jvmEnvOverlay: ConcurrentHashMap<String, String> = ConcurrentHashMap()

internal actual fun envVar(name: String): String? = jvmEnvOverlay[name] ?: System.getenv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    jvmEnvOverlay[name] = value
}

internal actual fun envVars(): List<Pair<String, String>> {
    val merged = LinkedHashMap<String, String>()
    merged.putAll(System.getenv())
    merged.putAll(jvmEnvOverlay)
    return merged.entries.map { (k, v) -> k to v }
}

internal actual fun currentDirectory(): String? = System.getProperty("user.dir")
