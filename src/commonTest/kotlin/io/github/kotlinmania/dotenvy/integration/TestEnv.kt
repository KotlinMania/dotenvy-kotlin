// port-lint: tests integration/util/testenv.rs
package io.github.kotlinmania.dotenvy.integration

import io.github.kotlinmania.dotenvy.TempDir
import io.github.kotlinmania.dotenvy.tempdir
import io.github.kotlinmania.io.buffered
import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import io.github.kotlinmania.io.writeString
import io.github.kotlinmania.oncecell.sync.OnceCell

public data class KeyVal(
    public val key: String,
    public val value: String,
)

public class TestEnv(
    private val tempDir: TempDir,
    private var workDir: Path,
    private val envVars: MutableList<KeyVal> = mutableListOf(),
    private var envfileContents: String? = null,
    private var envfilePath: Path,
) {
    public fun tempPath(): String = tempDir.path()

    public fun workDir(): Path = workDir

    public fun envVars(): List<KeyVal> = envVars.toList()

    public fun envfileContents(): String? = envfileContents

    public fun envfilePath(): Path = envfilePath

    public fun setEnvfileName(name: String): TestEnv {
        envfilePath = Path(tempDir.path(), name)
        return this
    }

    public fun setEnvfilePath(path: Path): TestEnv {
        envfilePath = path
        return this
    }

    public fun setEnvfileContents(contents: String): TestEnv {
        envfileContents = contents
        return this
    }

    public fun setWorkDir(path: Path): TestEnv {
        workDir = path
        return this
    }

    public fun addEnvVar(key: String, value: String): TestEnv {
        envVars.add(KeyVal(key, value))
        return this
    }

    public fun setEnvVars(vars: List<KeyVal>): TestEnv {
        envVars.clear()
        envVars.addAll(vars)
        return this
    }

    public fun writeEnvFile() {
        envfileContents?.let { contents ->
            SystemFileSystem.sink(envfilePath).buffered().use { sink ->
                sink.writeString(contents)
            }
        }
    }

    public fun close() {
        try {
            SystemFileSystem.delete(envfilePath, mustExist = false)
        } catch (_: Throwable) {
        }
        tempDir.close()
    }

    public companion object {
        private val envLocker: OnceCell<Map<String, String>> = OnceCell.new()

        public fun getLocker(): OnceCell<Map<String, String>> = envLocker

        public fun init(): TestEnv {
            val dir = tempdir().getOrThrow()
            val work = Path(dir.path())
            val envFile = Path(dir.path(), ".env")
            return TestEnv(
                tempDir = dir,
                workDir = work,
                envfilePath = envFile,
            )
        }

        public fun initWithEnvfile(contents: String): TestEnv {
            val env = init()
            env.setEnvfileContents(contents)
            return env
        }

        public fun default(): TestEnv {
            val dir = tempdir().getOrThrow()
            val work = Path(dir.path())
            val envFile = Path(dir.path(), ".env")
            val env =
                TestEnv(
                    tempDir = dir,
                    workDir = work,
                    envfilePath = envFile,
                )
            env.addEnvVar(TEST_EXISTING_KEY, TEST_EXISTING_VALUE)
            env.setEnvfileContents(createDefaultEnvfile())
            return env
        }
    }
}
