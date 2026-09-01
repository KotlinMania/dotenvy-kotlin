// port-lint: tests errors.rs
package io.github.kotlinmania.dotenvy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorsTest {
    @Test
    fun testIoErrorSource() {
        val err = Error.Io(IoError(IoErrorKind.PermissionDenied, "permission denied"))
        val source = err.source()
        assertNotNull(source)
        val ioErr = source as IoError
        assertEquals(IoErrorKind.PermissionDenied, ioErr.kind)
    }

    @Test
    fun testEnvvarErrorSource() {
        val err = Error.EnvVar(EnvVarError.NotPresent)
        val source = err.source()
        assertNotNull(source)
        val varErr = source as EnvVarError
        assertEquals(EnvVarError.NotPresent, varErr)
    }

    @Test
    fun testLineparseErrorSource() {
        val err = Error.LineParse("test line", 2)
        assertNull(err.source())
    }

    @Test
    fun testErrorNotFoundTrue() {
        val err = Error.Io(IoError(IoErrorKind.NotFound, "not found"))
        assertTrue(err.notFound())
    }

    @Test
    fun testErrorNotFoundFalse() {
        val err = Error.Io(IoError(IoErrorKind.PermissionDenied, "permission denied"))
        assertFalse(err.notFound())
    }

    @Test
    fun testIoErrorDisplay() {
        val ioErr = IoError(IoErrorKind.PermissionDenied, "permission denied")
        val err = Error.Io(ioErr)

        val errDesc = err.message
        val ioErrDesc = ioErr.message
        assertEquals(ioErrDesc, errDesc)
    }

    @Test
    fun testEnvvarErrorDisplay() {
        val varErr = EnvVarError.NotPresent
        val err = Error.EnvVar(varErr)

        val errDesc = err.message
        val varErrDesc = varErr.message
        assertEquals(varErrDesc, errDesc)
    }

    @Test
    fun testLineparseErrorDisplay() {
        val err = Error.LineParse("test line", 2)
        val errDesc = err.message
        assertEquals(
            "Error parsing line: 'test line', error at line index: 2",
            errDesc,
        )
    }
}
