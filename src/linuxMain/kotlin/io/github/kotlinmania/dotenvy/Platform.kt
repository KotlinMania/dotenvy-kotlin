package io.github.kotlinmania.dotenvy

import io.github.kotlinmania.libc.unix.getenv
import io.github.kotlinmania.libc.unix.setenv
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.getcwd

internal actual fun envVar(name: String): String? = getenv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    setenv(name, value, 1)
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentDirectory(): String? =
    memScoped {
        val size = 4096
        val buf = allocArray<ByteVar>(size)
        if (getcwd(buf, size.toULong()) == null) null else buf.toKString()
    }
