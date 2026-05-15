// port-lint: ignore (Android implementation of env/cwd shims for src/lib.rs and src/find.rs)
package io.github.kotlinmania.dotenvy

internal actual fun envVar(name: String): String? = System.getenv(name)

// Android (and the JVM more broadly) does not expose a portable way to mutate the running
// process's environment block. Reads in this process see the unmodified system environment;
// callers wanting to override values should use a different ingestion path (for example,
// a configuration map populated via [Iter]).
internal actual fun setEnvVar(name: String, value: String) {
    // intentionally empty
}

internal actual fun envVars(): List<Pair<String, String>> =
    System.getenv().entries.map { (k, v) -> k to v }

internal actual fun currentDirectory(): String? = System.getProperty("user.dir")
