package io.github.kotlinmania.dotenvy

import io.github.kotlinmania.libc.windows.getenv
import io.github.kotlinmania.libc.windows.putenvS
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix._getcwd

internal actual fun envVar(name: String): String? = getenv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    putenvS(name, value)
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentDirectory(): String? =
    memScoped {
        val size = 4096
        val buf = allocArray<ByteVar>(size)
        if (_getcwd(buf, size) == null) null else buf.toKString()
    }
