// port-lint: ignore (Android Native POSIX implementation of env/cwd shims for src/lib.rs and src/find.rs)
@file:OptIn(ExperimentalForeignApi::class)

package io.github.kotlinmania.dotenvy

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.setenv

internal actual fun envVar(name: String): String? = getenv(name)?.toKString()

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    setenv(name, value, 1)
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

internal actual fun currentDirectory(): String? = null
