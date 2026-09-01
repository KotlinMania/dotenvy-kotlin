package io.github.kotlinmania.dotenvy

internal actual fun envVar(name: String): String? {
    val raw: dynamic = jsGetEnv(name)
    return if (raw == null || raw == undefined()) null else raw.unsafeCast<String>()
}

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    jsSetEnv(name, value)
}

internal actual fun envVars(): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    val names = jsEnvKeys()
    val length = names.length
    for (i in 0 until length) {
        val key = names[i].unsafeCast<String>()
        val value = jsGetEnv(key)
        if (value != null && value != undefined()) {
            result.add(key to value.unsafeCast<String>())
        }
    }
    return result
}

internal actual fun currentDirectory(): String? {
    val raw: dynamic = jsCwd()
    return if (raw == null || raw == undefined()) null else raw.unsafeCast<String>()
}

private fun jsGetEnv(name: String): dynamic =
    js(
        "(typeof process !== 'undefined' && process && process.env) ? process.env[name] : undefined",
    )

private fun jsSetEnv(
    name: String,
    value: String,
): Unit =
    js(
        "if (typeof process !== 'undefined' && process && process.env) { process.env[name] = value; }",
    )

private fun jsEnvKeys(): dynamic =
    js(
        "(typeof process !== 'undefined' && process && process.env) ? Object.keys(process.env) : []",
    )

private fun jsCwd(): dynamic =
    js(
        "(typeof process !== 'undefined' && process && typeof process.cwd === 'function') ? process.cwd() : undefined",
    )

private fun undefined(): dynamic = js("undefined")
