// port-lint: source iter.rs
package io.github.kotlinmania.dotenvy

import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.indexOf
import kotlinx.io.readString

/**
 * An iterator yielding `(key, value)` pairs parsed from a buffered byte stream that contains
 * dotenv-style content.
 */
public class Iter internal constructor(
    private val source: Source,
) : Iterator<Result<Pair<String, String>>> {
    private val lines: QuotedLines = QuotedLines(source)
    private val substitutionData: MutableMap<String, String?> = mutableMapOf()

    private var nextItem: Result<Pair<String, String>>? = null
    private var ended: Boolean = false

    /** Wraps an unbuffered [RawSource] in a buffered view. */
    public constructor(reader: RawSource) : this(reader.buffered())

    /**
     * Loads all variables found in the `reader` into the environment,
     * preserving any existing environment variables of the same name.
     *
     * If a variable is specified multiple times within the reader's data,
     * then the first occurrence is applied.
     */
    public fun load(): Result<Unit> {
        val bom = removeBom()
        if (bom.isFailure) return Result.failure(bom.exceptionOrNull()!!)

        while (hasNext()) {
            val item = next()
            if (item.isFailure) return Result.failure(item.exceptionOrNull()!!)
            val (key, value) = item.getOrThrow()
            if (envVar(key) == null) {
                setEnvVar(key, value)
            }
        }

        return Result.success(Unit)
    }

    /**
     * Loads all variables found in the `reader` into the environment,
     * overriding any existing environment variables of the same name.
     *
     * If a variable is specified multiple times within the reader's data,
     * then the last occurrence is applied.
     */
    public fun loadOverride(): Result<Unit> {
        val bom = removeBom()
        if (bom.isFailure) return Result.failure(bom.exceptionOrNull()!!)

        while (hasNext()) {
            val item = next()
            if (item.isFailure) return Result.failure(item.exceptionOrNull()!!)
            val (key, value) = item.getOrThrow()
            setEnvVar(key, value)
        }

        return Result.success(Unit)
    }

    private fun removeBom(): Result<Unit> =
        try {
            if (source.request(3)) {
                val peeked = source.peek().buffered()
                val first = peeked.readByte()
                val second = peeked.readByte()
                val third = peeked.readByte()
                // https://www.compart.com/en/unicode/U+FEFF
                if (first == 0xEF.toByte() && second == 0xBB.toByte() && third == 0xBF.toByte()) {
                    // remove the BOM from the buffered source
                    source.skip(3)
                }
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(Error.Io(toIoError(e)))
        }

    override fun hasNext(): Boolean {
        if (ended) return false
        if (nextItem != null) return true
        nextItem = computeNext()
        if (nextItem == null) {
            ended = true
            return false
        }
        return true
    }

    override fun next(): Result<Pair<String, String>> {
        if (!hasNext()) throw NoSuchElementException()
        val item = nextItem!!
        nextItem = null
        return item
    }

    private fun computeNext(): Result<Pair<String, String>>? {
        while (true) {
            val rawLine = lines.next() ?: return null
            if (rawLine.isFailure) return Result.failure(rawLine.exceptionOrNull()!!)
            val line = rawLine.getOrThrow()

            val parsed = parseLine(line, substitutionData)
            if (parsed.isFailure) return Result.failure(parsed.exceptionOrNull()!!)
            val maybe = parsed.getOrThrow()
            if (maybe != null) return Result.success(maybe)
            // Ok(None) -> skip and read another line
        }
    }
}

private class QuotedLines(
    private val buf: Source,
) {
    fun next(): Result<String>? {
        val builder = StringBuilder()
        var curState: ParseState = ParseState.Complete
        while (true) {
            val bufPos = builder.length
            val readResult = readLineInto(buf, builder)
            if (readResult.isFailure) {
                return Result.failure(readResult.exceptionOrNull()!!)
            }
            val n = readResult.getOrThrow()
            if (n == 0) {
                return when (curState) {
                    ParseState.Complete -> null
                    else -> {
                        val len = builder.length
                        Result.failure(Error.LineParse(builder.toString(), len))
                    }
                }
            }
            // Skip lines which start with a # before iteration
            // This optimizes parsing a bit.
            if (builder.toString().trimStart().startsWith('#')) {
                return Result.success("")
            }
            val chunk = builder.substring(bufPos)
            val (curPos, nextState) = evalEndState(curState, chunk)
            curState = nextState

            when (curState) {
                ParseState.Complete -> {
                    if (builder.endsWith('\n')) {
                        builder.deleteAt(builder.length - 1)
                        if (builder.endsWith('\r')) {
                            builder.deleteAt(builder.length - 1)
                        }
                    }
                    return Result.success(builder.toString())
                }
                ParseState.Comment -> {
                    builder.setLength(bufPos + curPos)
                    return Result.success(builder.toString())
                }
                ParseState.Escape,
                ParseState.StrongOpen,
                ParseState.StrongOpenEscape,
                ParseState.WeakOpen,
                ParseState.WeakOpenEscape,
                ParseState.WhiteSpace,
                -> {
                    // continue reading more lines
                }
            }
        }
    }
}

private enum class ParseState {
    Complete,
    Escape,
    StrongOpen,
    StrongOpenEscape,
    WeakOpen,
    WeakOpenEscape,
    Comment,
    WhiteSpace,
}

private fun evalEndState(
    prevState: ParseState,
    buf: String,
): Pair<Int, ParseState> {
    var curState = prevState
    var curPos = 0

    for ((pos, c) in buf.withIndex()) {
        curPos = pos
        curState =
            when (curState) {
                ParseState.WhiteSpace ->
                    when (c) {
                        '#' -> return curPos to ParseState.Comment
                        '\\' -> ParseState.Escape
                        '"' -> ParseState.WeakOpen
                        '\'' -> ParseState.StrongOpen
                        else -> ParseState.Complete
                    }
                ParseState.Escape -> ParseState.Complete
                ParseState.Complete ->
                    when {
                        c.isWhitespace() && c != '\n' && c != '\r' -> ParseState.WhiteSpace
                        c == '\\' -> ParseState.Escape
                        c == '"' -> ParseState.WeakOpen
                        c == '\'' -> ParseState.StrongOpen
                        else -> ParseState.Complete
                    }
                ParseState.WeakOpen ->
                    when (c) {
                        '\\' -> ParseState.WeakOpenEscape
                        '"' -> ParseState.Complete
                        else -> ParseState.WeakOpen
                    }
                ParseState.WeakOpenEscape -> ParseState.WeakOpen
                ParseState.StrongOpen ->
                    when (c) {
                        '\\' -> ParseState.StrongOpenEscape
                        '\'' -> ParseState.Complete
                        else -> ParseState.StrongOpen
                    }
                ParseState.StrongOpenEscape -> ParseState.StrongOpen
                // Comments last the entire line.
                ParseState.Comment -> error("should have returned early")
            }
    }
    return curPos to curState
}

/**
 * Reads characters from [source] up to and including the next line-feed (or EOF), appending the
 * decoded text to [into]. Returns the number of characters appended; a return value of zero
 * indicates end-of-stream.
 */
private fun readLineInto(
    source: Source,
    into: StringBuilder,
): Result<Int> {
    return try {
        if (source.exhausted()) return Result.success(0)
        val nlByte = '\n'.code.toByte()
        val nlIndex = source.indexOf(nlByte)
        val text =
            if (nlIndex == -1L) {
                source.readString()
            } else {
                source.readString(nlIndex + 1)
            }
        into.append(text)
        Result.success(text.length)
    } catch (e: IOException) {
        Result.failure(Error.Io(toIoError(e)))
    }
}

internal fun toIoError(e: Throwable): IoError =
    when (e) {
        is IoError -> e
        is IOException -> IoError(IoErrorKind.Other, e.message ?: "io error", e)
        else -> IoError(IoErrorKind.Other, e.message ?: "io error", e)
    }
