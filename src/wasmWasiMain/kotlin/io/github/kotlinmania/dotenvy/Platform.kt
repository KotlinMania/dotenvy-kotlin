package io.github.kotlinmania.dotenvy

internal actual fun envVar(name: String): String? = null

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    // intentionally empty
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

// In standard WASI preview 1, the root preopened directory is typically mapped to ".".
internal actual fun currentDirectory(): String? = "."
