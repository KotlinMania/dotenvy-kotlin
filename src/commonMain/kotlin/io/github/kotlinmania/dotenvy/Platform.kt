// port-lint: ignore (platform abstractions over std::env and std::env::current_dir for src/lib.rs and src/find.rs)
package io.github.kotlinmania.dotenvy

/**
 * Returns the value of the named process environment variable, or null if the variable is
 * unset or the platform cannot decode it as Unicode. Mirrors the success arm of `env::var`.
 */
internal expect fun envVar(name: String): String?

/**
 * Sets the named process environment variable to [value]. Mirrors `env::set_var`.
 *
 * On platforms where the running process cannot mutate its own environment (notably Android
 * and browser-hosted Kotlin), this call is a no-op or affects only an in-process overlay,
 * see the per-target actual for behavior details.
 */
internal expect fun setEnvVar(name: String, value: String)

/** Returns a snapshot of the process environment as a list of (key, value) pairs. */
internal expect fun envVars(): List<Pair<String, String>>

/**
 * Returns the absolute path to the current working directory, or null if the platform does not
 * have one (browser hosts). Mirrors `env::current_dir` returning `Ok(PathBuf)`.
 */
internal expect fun currentDirectory(): String?
