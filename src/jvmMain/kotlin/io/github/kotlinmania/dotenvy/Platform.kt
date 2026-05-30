// port-lint: ignore (JVM implementation of env/cwd shims for src/lib.rs and src/find.rs)
package io.github.kotlinmania.dotenvy

internal actual fun envVar(name: String): String? = System.getenv(name)

internal actual fun setEnvVar(
    name: String,
    value: String,
) {
    // intentionally empty
}

internal actual fun envVars(): List<Pair<String, String>> = System.getenv().entries.map { (k, v) -> k to v }

internal actual fun currentDirectory(): String? = System.getProperty("user.dir")
