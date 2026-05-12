// port-lint: source src/find.rs
package io.github.kotlinmania.dotenvy

import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Builder that resolves a `.env`-style filename by walking from the current working directory
 * up through ancestor directories. Mirrors the upstream `pub struct Finder<'a>`.
 */
public class Finder internal constructor(private var filenamePath: Path) {

    /** Mirrors `Finder::new()`, defaulting the filename to `.env`. */
    public constructor() : this(Path(".env"))

    /** Mirrors `Finder::filename(filename)` and returns `self` for chaining. */
    public fun filename(filename: Path): Finder {
        this.filenamePath = filename
        return this
    }

    /**
     * Walks from the current directory up through its ancestors, returning the absolute path
     * to the first directory containing [filenamePath] together with an [Iter] over the
     * file's contents. Mirrors the upstream `pub fn find(self) -> Result<(PathBuf, Iter<File>)>`.
     */
    public fun find(): Result<Pair<Path, Iter>> {
        val cwdString = currentDirectory()
            ?: return Result.failure(
                Error.Io(IoError(IoErrorKind.Other, "current directory is unavailable"))
            )
        val pathResult = find(Path(cwdString), filenamePath)
        if (pathResult.isFailure) return Result.failure(pathResult.exceptionOrNull()!!)
        val path = pathResult.getOrThrow()
        val iter = try {
            Iter(SystemFileSystem.source(path).buffered())
        } catch (e: IOException) {
            return Result.failure(Error.Io(toIoError(e)))
        }
        return Result.success(path to iter)
    }
}

/** Searches for `filename` in `directory` and parent directories until found or root is reached. */
public fun find(directory: Path, filename: Path): Result<Path> {
    val candidate = Path(directory, filename.toString())

    val metadata = try {
        SystemFileSystem.metadataOrNull(candidate)
    } catch (e: IOException) {
        return Result.failure(Error.Io(toIoError(e)))
    }
    if (metadata != null) {
        if (metadata.isRegularFile) {
            return Result.success(candidate)
        }
    }

    val parent = directory.parent
    return if (parent != null) {
        find(parent, filename)
    } else {
        Result.failure(
            Error.Io(IoError(IoErrorKind.NotFound, "path not found"))
        )
    }
}
