@file:OptIn(ExperimentalForeignApi::class)

package io.github.kotlinmania.dotenvy

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix._getcwd
import platform.posix._putenv_s
import platform.posix.getenv

internal actual fun envVar(name: String): String? = getenv(name)?.toKString()

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    _putenv_s(name, value)
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

internal actual fun currentDirectory(): String? =
    memScoped {
        val size = 4096
        val buf = allocArray<ByteVar>(size)
        if (_getcwd(buf, size) == null) null else buf.toKString()
    }
