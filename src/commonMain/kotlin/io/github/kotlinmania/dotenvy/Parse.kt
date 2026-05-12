// port-lint: source src/parse.rs
package io.github.kotlinmania.dotenvy

// for readability's sake
internal typealias ParsedLine = Result<Pair<String, String>?>

internal fun parseLine(
    line: String,
    substitutionData: MutableMap<String, String?>,
): ParsedLine {
    val parser = LineParser(line, substitutionData)
    return parser.parseLine()
}

private class LineParser(
    val originalLine: String,
    val substitutionData: MutableMap<String, String?>,
) {
    // we don't want trailing whitespace
    var line: String = originalLine.trimEnd()
    var pos: Int = 0

    fun err(): Error = Error.LineParse(originalLine, pos)

    fun parseLine(): ParsedLine {
        skipWhitespace()
        // if its an empty line or a comment, skip it
        if (line.isEmpty() || line.startsWith('#')) {
            return Result.success(null)
        }

        val keyResult = parseKey()
        if (keyResult.isFailure) return Result.failure(keyResult.exceptionOrNull()!!)
        var key = keyResult.getOrThrow()
        skipWhitespace()

        // export can be either an optional prefix or a key itself
        if (key == "export") {
            // here we check for an optional `=`, below we throw directly when it's not found.
            if (expectEqual().isFailure) {
                val nextKey = parseKey()
                if (nextKey.isFailure) return Result.failure(nextKey.exceptionOrNull()!!)
                key = nextKey.getOrThrow()
                skipWhitespace()
                val eq = expectEqual()
                if (eq.isFailure) return Result.failure(eq.exceptionOrNull()!!)
            }
        } else {
            val eq = expectEqual()
            if (eq.isFailure) return Result.failure(eq.exceptionOrNull()!!)
        }
        skipWhitespace()

        if (line.isEmpty() || line.startsWith('#')) {
            substitutionData[key] = null
            return Result.success(key to "")
        }

        val parsedValue = parseValue(line, substitutionData)
        if (parsedValue.isFailure) return Result.failure(parsedValue.exceptionOrNull()!!)
        val value = parsedValue.getOrThrow()
        substitutionData[key] = value

        return Result.success(key to value)
    }

    fun parseKey(): Result<String> {
        if (line.isEmpty() || !(line[0].isAsciiAlphabetic() || line[0] == '_')) {
            return Result.failure(err())
        }
        val index = line.indexOfFirst { c ->
            !(c.isAsciiAlphanumeric() || c == '_' || c == '.')
        }.let { if (it < 0) line.length else it }
        pos += index
        val key = line.substring(0, index)
        line = line.substring(index)
        return Result.success(key)
    }

    fun expectEqual(): Result<Unit> {
        if (!line.startsWith('=')) {
            return Result.failure(err())
        }
        line = line.substring(1)
        pos += 1
        return Result.success(Unit)
    }

    fun skipWhitespace() {
        val index = line.indexOfFirst { c -> !c.isWhitespace() }
        if (index >= 0) {
            pos += index
            line = line.substring(index)
        } else {
            pos += line.length
            line = ""
        }
    }
}

private enum class SubstitutionMode {
    None,
    Block,
    EscapedBlock,
}

private fun parseValue(
    input: String,
    substitutionData: MutableMap<String, String?>,
): Result<String> {
    var strongQuote = false // '
    var weakQuote = false // "
    var escaped = false
    var expectingEnd = false

    //FIXME can this be done without yet another allocation per line?
    val output = StringBuilder()

    var substitutionMode = SubstitutionMode.None
    val substitutionName = StringBuilder()

    for ((index, c) in input.withIndex()) {
        //the regex _should_ already trim whitespace off the end
        //expectingEnd is meant to permit: k=v #comment
        //without affecting: k=v#comment
        //and throwing on: k=v w
        if (expectingEnd) {
            if (c == ' ' || c == '\t') {
                continue
            } else if (c == '#') {
                break
            } else {
                return Result.failure(Error.LineParse(input, index))
            }
        } else if (escaped) {
            //TODO I tried handling literal \r but various issues
            //imo not worth worrying about until there's a use case
            //(actually handling backslash 0x10 would be a whole other matter)
            //then there's \v \f bell hex... etc
            when (c) {
                '\\', '\'', '"', '$', ' ' -> output.append(c)
                'n' -> output.append('\n') // handle \n case
                else -> {
                    return Result.failure(Error.LineParse(input, index))
                }
            }

            escaped = false
        } else if (strongQuote) {
            if (c == '\'') {
                strongQuote = false
            } else {
                output.append(c)
            }
        } else if (substitutionMode != SubstitutionMode.None) {
            if (c.isAlphaNumericUnicode()) {
                substitutionName.append(c)
            } else {
                when (substitutionMode) {
                    SubstitutionMode.None -> error("unreachable")
                    SubstitutionMode.Block -> {
                        if (c == '{' && substitutionName.isEmpty()) {
                            substitutionMode = SubstitutionMode.EscapedBlock
                        } else {
                            applySubstitution(
                                substitutionData,
                                drainToString(substitutionName),
                                output,
                            )
                            if (c == '$') {
                                substitutionMode = if (!strongQuote && !escaped) {
                                    SubstitutionMode.Block
                                } else {
                                    SubstitutionMode.None
                                }
                            } else {
                                substitutionMode = SubstitutionMode.None
                                output.append(c)
                            }
                        }
                    }
                    SubstitutionMode.EscapedBlock -> {
                        if (c == '}') {
                            substitutionMode = SubstitutionMode.None
                            applySubstitution(
                                substitutionData,
                                drainToString(substitutionName),
                                output,
                            )
                        } else {
                            substitutionName.append(c)
                        }
                    }
                }
            }
        } else if (c == '$') {
            substitutionMode = if (!strongQuote && !escaped) {
                SubstitutionMode.Block
            } else {
                SubstitutionMode.None
            }
        } else if (weakQuote) {
            if (c == '"') {
                weakQuote = false
            } else if (c == '\\') {
                escaped = true
            } else {
                output.append(c)
            }
        } else if (c == '\'') {
            strongQuote = true
        } else if (c == '"') {
            weakQuote = true
        } else if (c == '\\') {
            escaped = true
        } else if (c == ' ' || c == '\t') {
            expectingEnd = true
        } else {
            output.append(c)
        }
    }

    //XXX also fail if escaped? or...
    return if (substitutionMode == SubstitutionMode.EscapedBlock || strongQuote || weakQuote) {
        val valueLength = input.length
        Result.failure(Error.LineParse(
            input,
            if (valueLength == 0) 0 else valueLength - 1,
        ))
    } else {
        applySubstitution(
            substitutionData,
            drainToString(substitutionName),
            output,
        )
        Result.success(output.toString())
    }
}

private fun applySubstitution(
    substitutionData: MutableMap<String, String?>,
    substitutionName: String,
    output: StringBuilder,
) {
    val environmentValue = envVar(substitutionName)
    if (environmentValue != null) {
        output.append(environmentValue)
    } else {
        val storedValue = substitutionData[substitutionName] ?: ""
        output.append(storedValue)
    }
}

private fun drainToString(buffer: StringBuilder): String {
    val s = buffer.toString()
    buffer.clear()
    return s
}

private fun Char.isAsciiAlphabetic(): Boolean = this in 'A'..'Z' || this in 'a'..'z'

private fun Char.isAsciiAlphanumeric(): Boolean =
    this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9'

// Mirrors Rust's `char::is_alphanumeric` (Unicode-aware), used for substitution-name characters.
private fun Char.isAlphaNumericUnicode(): Boolean = isLetterOrDigit()
