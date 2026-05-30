// port-lint: ignore (Wasm-WASI implementation of env/cwd shims for src/lib.rs and src/find.rs)
package io.github.kotlinmania.dotenvy

internal actual fun envVar(name: String): String? = null

internal actual fun setEnvVar(name: String, value: String) {
    // intentionally empty
}

internal actual fun envVars(): List<Pair<String, String>> = emptyList()

internal actual fun currentDirectory(): String? = null
