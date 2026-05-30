// port-lint: source src/lib.rs
// dotenv: https://crates.io/crates/dotenv
// A well-maintained fork of the dotenv crate.
//
// This library loads environment variables from a *.env* file. This is convenient for dev
// environments.

package io.github.kotlinmania.dotenvy

import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

private val startOnce: Unit by lazy {
    dotenv()
    Unit
}

private fun ensureStarted(): Unit = startOnce

/**
 * Gets the value for an environment variable.
 *
 * The value is `Result.success(s)` if the environment variable is present and decodable as a
 * Kotlin [String].
 *
 * Note: this function gets values from any visible environment variable key,
 * regardless of whether a *.env* file was loaded.
 *
 * Example:
 * ```
 * val value = dotenvy.`var`("HOME").getOrThrow()
 * println(value)  // prints `/home/foo`
 * ```
 */
public fun `var`(key: String): Result<String> {
    ensureStarted()
    val value =
        envVar(key)
            ?: return Result.failure(Error.EnvVar(EnvVarError.NotPresent))
    return Result.success(value)
}

/**
 * Returns a snapshot of `(key, value)` pairs for all environment variables of the current
 * process. The returned list contains a snapshot of the process's environment variables at
 * the time of invocation. Modifications to environment variables afterwards will not be
 * reflected.
 *
 * Example:
 * ```
 * val result: List<Pair<String, String>> = dotenvy.vars()
 * ```
 */
public fun vars(): List<Pair<String, String>> {
    ensureStarted()
    return envVars()
}

/**
 * Loads environment variables from the specified path.
 *
 * If variables with the same names already exist in the environment, then their values will be
 * preserved.
 *
 * Where multiple declarations for the same environment variable exist in your *.env*
 * file, the *first one* is applied.
 *
 * If you wish to ensure all variables are loaded from your *.env* file, ignoring variables
 * already existing in the environment, then use [fromPathOverride] instead.
 *
 * Example:
 * ```
 * dotenvy.fromPath(Path("path/to/.env")).getOrThrow()
 * ```
 */
public fun fromPath(path: Path): Result<Unit> {
    val iter = openIter(path)
    if (iter.isFailure) return Result.failure(iter.exceptionOrNull()!!)
    return iter.getOrThrow().load()
}

/**
 * Loads environment variables from the specified path,
 * overriding existing environment variables.
 *
 * Where multiple declarations for the same environment variable exist in your *.env* file, the
 * *last one* is applied.
 *
 * If you want the existing environment to take precedence,
 * or if you want to be able to override environment variables on the command line,
 * then use [fromPath] instead.
 *
 * Example:
 * ```
 * dotenvy.fromPathOverride(Path("path/to/.env")).getOrThrow()
 * ```
 */
public fun fromPathOverride(path: Path): Result<Unit> {
    val iter = openIter(path)
    if (iter.isFailure) return Result.failure(iter.exceptionOrNull()!!)
    return iter.getOrThrow().loadOverride()
}

/**
 * Returns an iterator over environment variables from the specified path.
 *
 * Example:
 * ```
 * for (item in dotenvy.fromPathIter(Path("path/to/.env")).getOrThrow()) {
 *   val (key, value) = item.getOrThrow()
 *   println("$key=$value")
 * }
 * ```
 */
public fun fromPathIter(path: Path): Result<Iter> = openIter(path)

/**
 * Loads environment variables from the specified file.
 *
 * If variables with the same names already exist in the environment, then their values will be
 * preserved.
 *
 * Where multiple declarations for the same environment variable exist in your *.env*
 * file, the *first one* is applied.
 *
 * If you wish to ensure all variables are loaded from your *.env* file, ignoring variables
 * already existing in the environment, then use [fromFilenameOverride] instead.
 *
 * Example:
 * ```
 * dotenvy.fromFilename("custom.env").getOrThrow()
 * ```
 *
 * It is also possible to load from a typical *.env* file like so. However, using [dotenv] is
 * preferred.
 *
 * ```
 * dotenvy.fromFilename(".env").getOrThrow()
 * ```
 */
public fun fromFilename(filename: Path): Result<Path> {
    val findResult = Finder().filename(filename).find()
    if (findResult.isFailure) return Result.failure(findResult.exceptionOrNull()!!)
    val (path, iter) = findResult.getOrThrow()
    val loadResult = iter.load()
    if (loadResult.isFailure) return Result.failure(loadResult.exceptionOrNull()!!)
    return Result.success(path)
}

/** Convenience overload accepting the filename as a [String]. */
public fun fromFilename(filename: String): Result<Path> = fromFilename(Path(filename))

/**
 * Loads environment variables from the specified file,
 * overriding existing environment variables.
 *
 * Where multiple declarations for the same environment variable exist in your *.env* file, the
 * *last one* is applied.
 *
 * If you want the existing environment to take precedence,
 * or if you want to be able to override environment variables on the command line,
 * then use [fromFilename] instead.
 *
 * Example:
 * ```
 * dotenvy.fromFilenameOverride("custom.env").getOrThrow()
 * ```
 *
 * It is also possible to load from a typical *.env* file like so. However, using
 * [dotenvOverride] is preferred.
 *
 * ```
 * dotenvy.fromFilenameOverride(".env").getOrThrow()
 * ```
 */
public fun fromFilenameOverride(filename: Path): Result<Path> {
    val findResult = Finder().filename(filename).find()
    if (findResult.isFailure) return Result.failure(findResult.exceptionOrNull()!!)
    val (path, iter) = findResult.getOrThrow()
    val loadResult = iter.loadOverride()
    if (loadResult.isFailure) return Result.failure(loadResult.exceptionOrNull()!!)
    return Result.success(path)
}

/** Convenience overload accepting the filename as a [String]. */
public fun fromFilenameOverride(filename: String): Result<Path> = fromFilenameOverride(Path(filename))

/**
 * Returns an iterator over environment variables from the specified file.
 *
 * Example:
 * ```
 * for (item in dotenvy.fromFilenameIter("custom.env").getOrThrow()) {
 *     val (key, value) = item.getOrThrow()
 *     println("$key=$value")
 * }
 * ```
 */
public fun fromFilenameIter(filename: Path): Result<Iter> {
    val findResult = Finder().filename(filename).find()
    if (findResult.isFailure) return Result.failure(findResult.exceptionOrNull()!!)
    val (_, iter) = findResult.getOrThrow()
    return Result.success(iter)
}

/** Convenience overload accepting the filename as a [String]. */
public fun fromFilenameIter(filename: String): Result<Iter> = fromFilenameIter(Path(filename))

/**
 * Loads environment variables from a [kotlinx.io.RawSource].
 *
 * This is useful for loading environment variables from IPC or the network.
 *
 * If variables with the same names already exist in the environment, then their values will be
 * preserved.
 *
 * Where multiple declarations for the same environment variable exist in your `reader`,
 * the *first one* is applied.
 *
 * If you wish to ensure all variables are loaded from your `reader`, ignoring variables
 * already existing in the environment, then use [fromReadOverride] instead.
 *
 * For regular files, use [fromPath] or [fromFilename].
 */
public fun fromRead(reader: RawSource): Result<Unit> {
    val iter = Iter(reader)
    val res = iter.load()
    if (res.isFailure) return Result.failure(res.exceptionOrNull()!!)
    return Result.success(Unit)
}

/**
 * Loads environment variables from a [kotlinx.io.RawSource],
 * overriding existing environment variables.
 *
 * This is useful for loading environment variables from IPC or the network.
 *
 * Where multiple declarations for the same environment variable exist in your `reader`, the
 * *last one* is applied.
 *
 * If you want the existing environment to take precedence,
 * or if you want to be able to override environment variables on the command line,
 * then use [fromRead] instead.
 *
 * For regular files, use [fromPathOverride] or [fromFilenameOverride].
 */
public fun fromReadOverride(reader: RawSource): Result<Unit> {
    val iter = Iter(reader)
    val res = iter.loadOverride()
    if (res.isFailure) return Result.failure(res.exceptionOrNull()!!)
    return Result.success(Unit)
}

/**
 * Returns an iterator over environment variables from a [kotlinx.io.RawSource].
 */
public fun fromReadIter(reader: RawSource): Iter = Iter(reader)

/**
 * Loads the *.env* file from the current directory or parents. This is typically what you want.
 *
 * If variables with the same names already exist in the environment, then their values will be
 * preserved.
 *
 * Where multiple declarations for the same environment variable exist in your *.env*
 * file, the *first one* is applied.
 *
 * If you wish to ensure all variables are loaded from your *.env* file, ignoring variables
 * already existing in the environment, then use [dotenvOverride] instead.
 *
 * An error will be returned if the file is not found.
 */
public fun dotenv(): Result<Path> {
    val findResult = Finder().find()
    if (findResult.isFailure) return Result.failure(findResult.exceptionOrNull()!!)
    val (path, iter) = findResult.getOrThrow()
    val loadResult = iter.load()
    if (loadResult.isFailure) return Result.failure(loadResult.exceptionOrNull()!!)
    return Result.success(path)
}

/**
 * Loads all variables found in the `reader` into the environment,
 * overriding any existing environment variables of the same name.
 *
 * Where multiple declarations for the same environment variable exist in your *.env* file, the
 * *last one* is applied.
 *
 * If you want the existing environment to take precedence,
 * or if you want to be able to override environment variables on the command line,
 * then use [dotenv] instead.
 */
public fun dotenvOverride(): Result<Path> {
    val findResult = Finder().find()
    if (findResult.isFailure) return Result.failure(findResult.exceptionOrNull()!!)
    val (path, iter) = findResult.getOrThrow()
    val loadResult = iter.loadOverride()
    if (loadResult.isFailure) return Result.failure(loadResult.exceptionOrNull()!!)
    return Result.success(path)
}

/**
 * Returns an iterator over environment variables.
 *
 * Example:
 * ```
 * for (item in dotenvy.dotenvIter().getOrThrow()) {
 *     val (key, value) = item.getOrThrow()
 *     println("$key=$value")
 * }
 * ```
 */
public fun dotenvIter(): Result<Iter> {
    val findResult = Finder().find()
    if (findResult.isFailure) return Result.failure(findResult.exceptionOrNull()!!)
    val (_, iter) = findResult.getOrThrow()
    return Result.success(iter)
}

private fun openIter(path: Path): Result<Iter> =
    try {
        Result.success(Iter(SystemFileSystem.source(path).buffered()))
    } catch (e: IOException) {
        Result.failure(Error.Io(toIoError(e)))
    }
