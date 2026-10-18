package io.github.kotlinmania.dotenvy

import io.github.kotlinmania.io.buffered
import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import io.github.kotlinmania.io.writeString
import io.github.kotlinmania.rand.rngs.rng

public class TempDir(
    private val pathValue: String,
) {
    public fun path(): String = pathValue

    public fun close() {
        try {
            SystemFileSystem.delete(Path(pathValue, ".env"), mustExist = false)
            SystemFileSystem.delete(Path(pathValue, "custom.env"), mustExist = false)
            SystemFileSystem.delete(Path(pathValue), mustExist = false)
        } catch (_: Throwable) {
            // best effort cleanup
        }
    }
}

public fun tempdir(): Result<TempDir> {
    val base = currentDirectory() ?: return Result.failure(IllegalStateException("Filesystem not available in this environment"))
    val name = "dotenvy_test_" + rng().nextU64().toString(16)
    val dirPath = Path(base, name)
    SystemFileSystem.createDirectories(dirPath)
    return Result.success(TempDir(dirPath.toString()))
}

public fun tempdirWithDotenv(dotenvText: String): TempDir {
    setEnvVar("EXISTING", "from_env")
    val dir = tempdir().getOrThrow()
    val dotenvPath = Path(dir.path(), ".env")
    SystemFileSystem.sink(dotenvPath).buffered().use { sink ->
        sink.writeString(dotenvText)
    }
    return dir
}

public fun makeTestDotenv(): TempDir = tempdirWithDotenv("TESTKEY=test_val\nTESTKEY=test_val_overridden\nEXISTING=from_file")
