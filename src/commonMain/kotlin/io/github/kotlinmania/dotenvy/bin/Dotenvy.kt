// port-lint: source bin/dotenvy.rs
package io.github.kotlinmania.dotenvy.bin

import io.github.kotlinmania.clap.Arg
import io.github.kotlinmania.clap.Command
import io.github.kotlinmania.dotenvy.dotenv
import io.github.kotlinmania.dotenvy.fromFilename

public data class ProcessCommand(
    public val name: String,
    public val args: List<String>,
)

public fun makeCommand(name: String, args: List<String>): ProcessCommand =
    ProcessCommand(name, args)

public fun buildCli(): Command =
    Command
        .new("dotenvy")
        .about("Run a command using the environment in a .env file")
        .allowExternalSubcommands(true)
        .argRequiredElseHelp(true)
        .arg(
            Arg
                .new("FILE")
                .short('f')
                .long("file")
                .help("Use a specific .env file (defaults to .env)"),
        )

public fun runCli(args: List<String>): Result<ProcessCommand> {
    val matches = buildCli().getMatchesFrom(args)
    val file = matches.getOneString("FILE")
    val loadResult =
        if (file == null) {
            dotenv()
        } else {
            fromFilename(file)
        }
    if (loadResult.isFailure) {
        return Result.failure(loadResult.exceptionOrNull()!!)
    }

    val subcommand =
        matches.subcommand()
            ?: return Result.failure(IllegalArgumentException("error: missing required argument <COMMAND>"))

    val (name, subMatches) = subcommand
    val subArgs =
        subMatches.getManyStrings("").takeUnless { it.isNullOrEmpty() }
            ?: args.dropWhile { it != name }.drop(1)
    return Result.success(makeCommand(name, subArgs))
}

public fun main(args: Array<String>) {
    runCli(args.toList()).fold(
        onSuccess = { command ->
            println("Running command: ${command.name} with args ${command.args}")
        },
        onFailure = { error ->
            println("fatal: ${error.message}")
        },
    )
}
