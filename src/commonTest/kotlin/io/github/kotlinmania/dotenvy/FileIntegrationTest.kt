// port-lint: tests test-from-path.rs
package io.github.kotlinmania.dotenvy

import io.github.kotlinmania.dotenvy.bin.makeCommand
import io.github.kotlinmania.dotenvy.bin.runCli
import io.github.kotlinmania.dotenvy.integration.TEST_EXISTING_KEY
import io.github.kotlinmania.dotenvy.integration.TEST_EXISTING_VALUE
import io.github.kotlinmania.dotenvy.integration.TestEnv
import io.github.kotlinmania.io.buffered
import io.github.kotlinmania.io.files.Path
import io.github.kotlinmania.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileIntegrationTest {
    private fun isFilesystemAvailable(): Boolean = currentDirectory() != null

    @Test
    fun testFromPathIter() {
        if (!isFilesystemAvailable()) return
        val dir = makeTestDotenv()
        try {
            val path = Path(dir.path(), ".env")
            val iter = fromPathIter(path).getOrThrow()

            val pairs = mutableListOf<Pair<String, String>>()
            while (iter.hasNext()) {
                pairs.add(iter.next().getOrThrow())
            }

            assertEquals(3, pairs.size)
            assertEquals("TESTKEY" to "test_val", pairs[0])
            assertEquals("TESTKEY" to "test_val_overridden", pairs[1])
            assertEquals("EXISTING" to "from_file", pairs[2])
        } finally {
            dir.close()
        }
    }

    @Test
    fun testFromPathAndOverride() {
        if (!isFilesystemAvailable()) return
        val dir = makeTestDotenv()
        try {
            val path = Path(dir.path(), ".env")

            val loadRes = fromPath(path)
            assertTrue(loadRes.isSuccess)

            val overrideRes = fromPathOverride(path)
            assertTrue(overrideRes.isSuccess)
        } finally {
            dir.close()
        }
    }

    @Test
    fun testFromReadIter() {
        if (!isFilesystemAvailable()) return
        val dir = makeTestDotenv()
        try {
            val path = Path(dir.path(), ".env")
            val source = SystemFileSystem.source(path).buffered()
            val iter = fromReadIter(source)

            val pairs = mutableListOf<Pair<String, String>>()
            while (iter.hasNext()) {
                pairs.add(iter.next().getOrThrow())
            }

            assertEquals(3, pairs.size)
            assertEquals("TESTKEY" to "test_val", pairs[0])
            assertEquals("TESTKEY" to "test_val_overridden", pairs[1])
            assertEquals("EXISTING" to "from_file", pairs[2])
        } finally {
            dir.close()
        }
    }

    @Test
    fun testFromRead() {
        if (!isFilesystemAvailable()) return
        val dir = makeTestDotenv()
        try {
            val path = Path(dir.path(), ".env")
            val source = SystemFileSystem.source(path).buffered()
            val res = fromRead(source)
            assertTrue(res.isSuccess)
        } finally {
            dir.close()
        }
    }

    @Test
    fun testFromReadOverride() {
        if (!isFilesystemAvailable()) return
        val dir = makeTestDotenv()
        try {
            val path = Path(dir.path(), ".env")
            val source = SystemFileSystem.source(path).buffered()
            val res = fromReadOverride(source)
            assertTrue(res.isSuccess)
        } finally {
            dir.close()
        }
    }

    @Test
    fun testIgnoreBom() {
        if (!isFilesystemAvailable()) return
        val bom = "\uFEFF"
        val dir = tempdirWithDotenv("${bom}TESTKEY=test_val")
        try {
            val path = Path(dir.path(), ".env")
            val iter = fromPathIter(path).getOrThrow()
            assertTrue(iter.hasNext())
            val item = iter.next().getOrThrow()
            assertEquals("TESTKEY" to "test_val", item)
            assertFalse(iter.hasNext())
        } finally {
            dir.close()
        }
    }

    @Test
    fun testMultiline() {
        if (!isFilesystemAvailable()) return
        val value = "-----BEGIN PRIVATE KEY-----\n-----END PRIVATE KEY-----\\n\\\"QUOTED\\\""
        val weak = "-----BEGIN PRIVATE KEY-----\n-----END PRIVATE KEY-----\n\"QUOTED\""
        val dir =
            tempdirWithDotenv(
                "KEY=my\\ cool\\ value\n" +
                    "KEY3=\"awesome \\\"stuff\\\"\nmore\non other\nlines\"\n" +
                    "KEY4='hello '\\''world'\"\ngood ' \\'morning\"\n" +
                    "WEAK=\"$value\"\n" +
                    "STRONG='$value'\n",
            )
        try {
            val path = Path(dir.path(), ".env")
            val iter = fromPathIter(path).getOrThrow()
            val map = mutableMapOf<String, String>()
            while (iter.hasNext()) {
                val (k, v) = iter.next().getOrThrow()
                map[k] = v
            }

            assertEquals("my cool value", map["KEY"])
            assertEquals("awesome \"stuff\"\nmore\non other\nlines", map["KEY3"])
            assertEquals("hello 'world\ngood ' 'morning", map["KEY4"])
            assertEquals(weak, map["WEAK"])
            assertEquals(value, map["STRONG"])
        } finally {
            dir.close()
        }
    }

    @Test
    fun testMultilineComment() {
        if (!isFilesystemAvailable()) return
        val dir =
            tempdirWithDotenv(
                """
                # Start of .env file
                # Comment line with single ' quote
                # Comment line with double " quote
                 # Comment line with double " quote and starts with a space
                TESTKEY1=test_val # 1 '" comment
                TESTKEY2=test_val_with_#_hash # 2 '" comment
                TESTKEY3="test_val quoted with # hash" # 3 '" comment
                TESTKEY4="Line 1
                # Line 2
                Line 3" # 4 Multiline "' comment
                TESTKEY5="Line 4
                # Line 5
                Line 6
                " # 5 Multiline "' comment
                # End of .env file
                """.trimIndent(),
            )
        try {
            val path = Path(dir.path(), ".env")
            val iter = fromPathIter(path).getOrThrow()
            val map = mutableMapOf<String, String>()
            while (iter.hasNext()) {
                val (k, v) = iter.next().getOrThrow()
                map[k] = v
            }

            assertEquals("test_val", map["TESTKEY1"])
            assertEquals("test_val_with_#_hash", map["TESTKEY2"])
            assertEquals("test_val quoted with # hash", map["TESTKEY3"])
            assertEquals("Line 1\n# Line 2\nLine 3", map["TESTKEY4"])
            assertEquals("Line 4\n# Line 5\nLine 6\n", map["TESTKEY5"])
        } finally {
            dir.close()
        }
    }

    @Test
    fun testCliMatching() {
        if (!isFilesystemAvailable()) return
        val cmd = makeCommand("echo", listOf("hello", "world"))
        assertEquals("echo", cmd.name)
        assertEquals(listOf("hello", "world"), cmd.args)

        val dir = tempdirWithDotenv("CLI_TEST_KEY=cli_val")
        try {
            val envFile = Path(dir.path(), ".env")
            val runResult = runCli(listOf("dotenvy", "-f", envFile.toString(), "echo", "arg1"))
            assertTrue(runResult.isSuccess)
            val proc = runResult.getOrThrow()
            assertEquals("echo", proc.name)
            assertEquals(listOf("arg1"), proc.args)
        } finally {
            dir.close()
        }
    }

    @Test
    fun testTestEnvIntegration() {
        if (!isFilesystemAvailable()) return
        val testEnv = TestEnv.default()
        try {
            assertEquals(1, testEnv.envVars().size)
            assertEquals(TEST_EXISTING_KEY, testEnv.envVars()[0].key)
            assertEquals(TEST_EXISTING_VALUE, testEnv.envVars()[0].value)

            testEnv.writeEnvFile()
            assertTrue(SystemFileSystem.exists(testEnv.envfilePath()))

            val locker = TestEnv.getLocker()
            val map = locker.getOrInit { mapOf("LOCK" to "1") }
            assertEquals("1", map["LOCK"])
        } finally {
            testEnv.close()
        }
    }
}
