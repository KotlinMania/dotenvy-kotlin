@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.kotlinmania.dotenvy

internal actual fun envVar(name: String): String? = jsGetEnv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    jsSetEnv(name, value)
}

internal actual fun envVars(): List<Pair<String, String>> {
    val n = jsEnvCount()
    val result = ArrayList<Pair<String, String>>(n)
    for (i in 0 until n) {
        val key = jsEnvKeyAt(i) ?: continue
        val value = jsGetEnv(key) ?: continue
        result.add(key to value)
    }
    return result
}

internal actual fun currentDirectory(): String? = jsCwd()

private fun jsGetEnv(name: String): String? =
    js(
        "(typeof process !== 'undefined' && process && process.env && typeof process.env[name] === 'string') ? process.env[name] : null",
    )

private fun jsSetEnv(
    name: String,
    value: String,
) {
    js("if (typeof process !== 'undefined' && process && process.env) { process.env[name] = value; }")
}

private fun jsEnvCount(): Int =
    js(
        "(typeof process !== 'undefined' && process && process.env) ? Object.keys(process.env).length : 0",
    )

private fun jsEnvKeyAt(index: Int): String? =
    js(
        "(typeof process !== 'undefined' && process && process.env) ? Object.keys(process.env)[index] : null",
    )

private fun jsCwd(): String? =
    js(
        "(typeof process !== 'undefined' && process && typeof process.cwd === 'function') ? process.cwd() : null",
    )
