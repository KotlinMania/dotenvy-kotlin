package io.github.kotlinmania.dotenvy

import io.github.kotlinmania.libc.wasi.getenv
import io.github.kotlinmania.libc.wasi.setenv

internal actual fun envVar(name: String): String? = getenv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    setenv(name, value, 1)
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

// In standard WASI preview 1, the root preopened directory is typically mapped to ".".
internal actual fun currentDirectory(): String? = "."
