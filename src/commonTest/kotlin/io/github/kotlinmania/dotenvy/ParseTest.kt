// port-lint: source src/parse.rs
package io.github.kotlinmania.dotenvy

import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

private fun iterFromString(text: String): Iter {
    val buffer = Buffer()
    buffer.writeString(text)
    return Iter(buffer)
}

private fun collectAll(iter: Iter): List<Result<Pair<String, String>>> {
    val out = mutableListOf<Result<Pair<String, String>>>()
    while (iter.hasNext()) {
        out.add(iter.next())
    }
    return out
}

class ParseLineTest {
    @Test
    fun testParseLineEnv() {
        // Note 5 spaces after 'KEY8=' below
        val input =
            "\n" +
                "KEY=1\n" +
                "KEY2=\"2\"\n" +
                "KEY3='3'\n" +
                "KEY4='fo ur'\n" +
                "KEY5=\"fi ve\"\n" +
                "KEY6=s\\ ix\n" +
                "KEY7=\n" +
                "KEY8=     \n" +
                "KEY9=   # foo\n" +
                "KEY10  =\"whitespace before =\"\n" +
                "KEY11=    \"whitespace after =\"\n" +
                "export=\"export as key\"\n" +
                "export   SHELL_LOVER=1\n"

        val actualIter = iterFromString(input)

        val expected =
            listOf(
                "KEY" to "1",
                "KEY2" to "2",
                "KEY3" to "3",
                "KEY4" to "fo ur",
                "KEY5" to "fi ve",
                "KEY6" to "s ix",
                "KEY7" to "",
                "KEY8" to "",
                "KEY9" to "",
                "KEY10" to "whitespace before =",
                "KEY11" to "whitespace after =",
                "export" to "export as key",
                "SHELL_LOVER" to "1",
            )

        var count = 0
        val actuals = collectAll(actualIter)
        for ((expectedItem, actual) in expected.zip(actuals)) {
            assertTrue(actual.isSuccess, "expected success but got $actual")
            assertEquals(expectedItem, actual.getOrThrow())
            count += 1
        }

        assertEquals(13, count)
    }

    @Test
    fun testParseLineComment() {
        val input =
            "\n" +
                "# foo=bar\n" +
                "#    "
        val actuals = collectAll(iterFromString(input))
        // empty input followed by comment-only lines should yield no parse results
        val nonEmptyResults =
            actuals.filter { result ->
                result.isFailure || result.getOrThrow().first.isNotEmpty()
            }
        assertTrue(nonEmptyResults.isEmpty(), "expected no non-empty results, got $nonEmptyResults")
    }

    @Test
    fun testParseLineInvalid() {
        // Note 4 spaces after 'invalid' below
        val input =
            "\n" +
                "  invalid    \n" +
                "very bacon = yes indeed\n" +
                "=value"
        val actuals = collectAll(iterFromString(input))

        var count = 0
        for (actual in actuals) {
            assertTrue(actual.isFailure, "expected failure for $actual")
            count += 1
        }
        assertEquals(3, count)
    }

    @Test
    fun testParseValueEscapes() {
        val input =
            "\n" +
                "KEY=my\\ cool\\ value\n" +
                "KEY2=\\\$sweet\n" +
                "KEY3=\"awesome stuff \\\"mang\\\"\"\n" +
                "KEY4='sweet \$\\fgs'\\''fds'\n" +
                "KEY5=\"'\\\"yay\\\\\"\\ \"stuff\"\n" +
                "KEY6=\"lol\" #well you see when I say lol wh\n" +
                "KEY7=\"line 1\\nline 2\"\n"

        val actuals = collectAll(iterFromString(input))

        val expected =
            listOf(
                "KEY" to "my cool value",
                "KEY2" to "\$sweet",
                "KEY3" to "awesome stuff \"mang\"",
                "KEY4" to "sweet \$\\fgs'fds",
                "KEY5" to "'\"yay\\ stuff",
                "KEY6" to "lol",
                "KEY7" to "line 1\nline 2",
            )

        for ((expectedItem, actual) in expected.zip(actuals)) {
            assertTrue(actual.isSuccess, "expected success but got $actual")
            assertEquals(expectedItem, actual.getOrThrow())
        }
    }

    @Test
    fun testParseValueEscapesInvalid() {
        val input =
            "\n" +
                "KEY=my uncool value\n" +
                "KEY2=\"why\n" +
                "KEY3='please stop''\n" +
                "KEY4=h\\8u\n"

        val actuals = collectAll(iterFromString(input))
        for (actual in actuals) {
            assertTrue(actual.isFailure, "expected failure but got $actual")
        }
    }
}

class VariableSubstitutionTest {
    private fun assertParsedString(
        input: String,
        expected: List<Pair<String, String>>,
    ) {
        val actuals = collectAll(iterFromString(input))
        val expectedCount = expected.size

        var count = 0
        for ((expectedItem, actual) in expected.zip(actuals)) {
            assertTrue(actual.isSuccess, "expected success but got $actual")
            assertEquals(expectedItem, actual.getOrThrow())
            count += 1
        }
        assertEquals(expectedCount, count)
    }

    @Test
    fun variableInParenthesisSurroundedByQuotes() {
        assertParsedString(
            "\n            KEY=test\n            KEY1=\"\${KEY}\"\n            ",
            listOf("KEY" to "test", "KEY1" to "test"),
        )
    }

    @Test
    fun substituteUndefinedVariablesToEmptyString() {
        assertParsedString(
            "KEY=\">\$KEY1<>\${KEY2}<\"",
            listOf("KEY" to "><><"),
        )
    }

    @Test
    fun doNotSubstituteVariablesWithDollarEscaped() {
        assertParsedString(
            "KEY=>\\\$KEY1<>\\\${KEY2}<",
            listOf("KEY" to ">\$KEY1<>\${KEY2}<"),
        )
    }

    @Test
    fun doNotSubstituteVariablesInWeakQuotesWithDollarEscaped() {
        assertParsedString(
            "KEY=\">\\\$KEY1<>\\\${KEY2}<\"",
            listOf("KEY" to ">\$KEY1<>\${KEY2}<"),
        )
    }

    @Test
    fun doNotSubstituteVariablesInStrongQuotes() {
        assertParsedString(
            "KEY='>\${KEY1}<>\$KEY2<'",
            listOf("KEY" to ">\${KEY1}<>\$KEY2<"),
        )
    }

    @Test
    fun sameVariableReused() {
        assertParsedString(
            "\n    KEY=VALUE\n    KEY1=\$KEY\$KEY\n    ",
            listOf("KEY" to "VALUE", "KEY1" to "VALUEVALUE"),
        )
    }

    @Test
    fun withDot() {
        assertParsedString(
            "\n    KEY.Value=VALUE\n    ",
            listOf("KEY.Value" to "VALUE"),
        )
    }

    @Test
    fun recursiveSubstitution() {
        assertParsedString(
            "\n            KEY=\${KEY1}+KEY_VALUE\n            KEY1=\${KEY}+KEY1_VALUE\n            ",
            listOf("KEY" to "+KEY_VALUE", "KEY1" to "+KEY_VALUE+KEY1_VALUE"),
        )
    }

    @Test
    fun variableWithoutParenthesisIsSubstitutedBeforeSeparators() {
        assertParsedString(
            "\n            KEY1=test_user\n" +
                "            KEY1_1=test_user_with_separator\n" +
                "            KEY=\">\$KEY1_1<>\$KEY1}<>\$KEY1{<\"\n            ",
            listOf(
                "KEY1" to "test_user",
                "KEY1_1" to "test_user_with_separator",
                "KEY" to ">test_user_1<>test_user}<>test_user{<",
            ),
        )
    }

    @Test
    fun consequentSubstitutions() {
        assertParsedString(
            "\n    KEY1=test_user\n    KEY2=\$KEY1_2\n    KEY=>\${KEY1}<>\${KEY2}<\n    ",
            listOf(
                "KEY1" to "test_user",
                "KEY2" to "test_user_2",
                "KEY" to ">test_user<>test_user_2<",
            ),
        )
    }

    @Test
    fun consequentSubstitutionsWithOneMissing() {
        assertParsedString(
            "\n    KEY2=\$KEY1_2\n    KEY=>\${KEY1}<>\${KEY2}<\n    ",
            listOf("KEY2" to "_2", "KEY" to "><>_2<"),
        )
    }
}

class ParseErrorTest {
    @Test
    fun shouldNotParseUnfinishedSubstitutions() {
        val wrongValue = ">\${KEY{<"

        val parsedValues =
            collectAll(
                iterFromString("\n    KEY=VALUE\n    KEY1=$wrongValue\n    "),
            )

        assertEquals(2, parsedValues.size)

        val first = parsedValues[0]
        if (first.isSuccess) {
            assertEquals("KEY" to "VALUE", first.getOrThrow())
        } else {
            fail("Expected the first value to be parsed")
        }

        val second = parsedValues[1]
        val secondErr = second.exceptionOrNull()
        if (secondErr is Error.LineParse) {
            assertEquals(wrongValue, secondErr.line)
            assertEquals(wrongValue.length - 1, secondErr.errorIndex)
        } else {
            fail("Expected the second value not to be parsed; got $second")
        }
    }

    @Test
    fun shouldNotAllowDotAsFirstCharacterOfKey() {
        val wrongKeyValue = ".Key=VALUE"

        val parsedValues = collectAll(iterFromString(wrongKeyValue))

        assertEquals(1, parsedValues.size)

        val err = parsedValues[0].exceptionOrNull()
        if (err is Error.LineParse) {
            assertEquals(wrongKeyValue, err.line)
            assertEquals(0, err.errorIndex)
        } else {
            fail("Expected the second value not to be parsed; got ${parsedValues[0]}")
        }
    }

    @Test
    fun shouldNotParseIllegalFormat() {
        val wrongFormat = "<><><>"
        val parsedValues = collectAll(iterFromString(wrongFormat))

        assertEquals(1, parsedValues.size)

        val err = parsedValues[0].exceptionOrNull()
        if (err is Error.LineParse) {
            assertEquals(wrongFormat, err.line)
            assertEquals(0, err.errorIndex)
        } else {
            fail("Expected the second value not to be parsed; got ${parsedValues[0]}")
        }
    }

    @Test
    fun shouldNotParseIllegalEscape() {
        val wrongEscape = ">\\f<"
        val parsedValues = collectAll(iterFromString("VALUE=$wrongEscape"))

        assertEquals(1, parsedValues.size)

        val err = parsedValues[0].exceptionOrNull()
        if (err is Error.LineParse) {
            assertEquals(wrongEscape, err.line)
            assertEquals(wrongEscape.indexOf('\\') + 1, err.errorIndex)
        } else {
            fail("Expected the second value not to be parsed; got ${parsedValues[0]}")
        }
    }
}
