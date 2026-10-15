package io.github.kotlinmania.dotenvy

import io.github.kotlinmania.libc.unix.getenv
import io.github.kotlinmania.libc.unix.setenv
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager

internal actual fun envVar(name: String): String? = getenv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    setenv(name, value, 1)
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentDirectory(): String? = NSFileManager.defaultManager.currentDirectoryPath
