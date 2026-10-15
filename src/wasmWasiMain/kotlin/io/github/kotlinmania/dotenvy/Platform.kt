package io.github.kotlinmania.dotenvy

private val wasiEnvOverlay: MutableMap<String, String> = mutableMapOf()

internal actual fun envVar(name: String): String? = wasiEnvOverlay[name]

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    wasiEnvOverlay[name] = value
}

internal actual fun envVars(): List<Pair<String, String>> =
    wasiEnvOverlay.entries.map { (k, v) -> k to v }

// In standard WASI preview 1, the root preopened directory is typically mapped to ".".
internal actual fun currentDirectory(): String? = "."
