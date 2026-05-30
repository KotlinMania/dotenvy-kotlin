@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
// port-lint: source src/errors.rs

package io.github.kotlinmania.dotenvy

import kotlinx.io.IOException
import kotlin.native.HiddenFromObjC

/**
 * The result type used by all fallible dotenvy operations. The error case carries an [Error]
 * value, which is itself a [Throwable] and is wrapped in [kotlin.Result] via
 * [Result.failure].
 */
public typealias Result<T> = kotlin.Result<T>

/**
 * Categories of I/O failure that dotenvy distinguishes. Mirrors the subset of `io::ErrorKind`
 * that the upstream crate observes on the matched paths.
 */
public enum class IoErrorKind {
    NotFound,
    PermissionDenied,
    Other,
}

/**
 * A platform-neutral I/O error that carries an [IoErrorKind] tag. This stands in for
 * `std::io::Error` in the upstream crate so that callers can distinguish [IoErrorKind.NotFound]
 * the same way they would in Rust.
 */
@HiddenFromObjC
public open class IoError(
    public val kind: IoErrorKind,
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)

/**
 * Variants describing why looking up an environment variable failed. Mirrors `env::VarError`.
 * The [NotUnicode] variant is preserved for parity even though Kotlin strings are always
 * Unicode; native bridges that decode platform-specific strings may choose to emit it.
 */
@HiddenFromObjC
public sealed class EnvVarError(
    message: String,
) : RuntimeException(message) {
    /** The environment variable was not present. */
    public object NotPresent : EnvVarError("environment variable not found")

    /** The environment variable was not valid Unicode. */
    public class NotUnicode(
        public val raw: String,
    ) : EnvVarError(
            "environment variable was not valid unicode: \"$raw\"",
        )
}

/**
 * The error type produced by dotenvy. This is a sealed translation of the upstream `enum Error`,
 * preserving the same three variants. The Rust type carries a `non_exhaustive` attribute, so
 * additional subclasses may be introduced without breaking source compatibility.
 */
@HiddenFromObjC
public sealed class Error(
    message: String,
    cause: Throwable? = null,
) : Throwable(message, cause) {
    /**
     * A line in the .env input could not be parsed. [line] is the offending line and
     * [errorIndex] is the byte offset within the line at which parsing failed.
     */
    public class LineParse(
        public val line: String,
        public val errorIndex: Int,
    ) : Error(
            "Error parsing line: '$line', error at line index: $errorIndex",
        )

    /** A wrapped [IoError]. */
    public class Io(
        public val ioError: IoError,
    ) : Error(
            ioError.message ?: "io error",
            ioError,
        )

    /** A wrapped [EnvVarError]. */
    public class EnvVar(
        public val varError: EnvVarError,
    ) : Error(
            varError.message ?: "env var error",
            varError,
        )

    /** Returns true if this error is an [Io] error whose kind is [IoErrorKind.NotFound]. */
    public fun notFound(): Boolean {
        if (this is Io) {
            return ioError.kind == IoErrorKind.NotFound
        }
        return false
    }

    /**
     * Returns the underlying source error, if any. Mirrors `error::Error::source`.
     * [LineParse] has no source; [Io] returns the wrapped [IoError]; [EnvVar] returns the
     * wrapped [EnvVarError].
     */
    public fun source(): Throwable? =
        when (this) {
            is Io -> ioError
            is EnvVar -> varError
            is LineParse -> null
        }
}
