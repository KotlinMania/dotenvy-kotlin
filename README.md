# dotenvy-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fdotenvy--kotlin-blue.svg)](https://github.com/KotlinMania/dotenvy-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/dotenvy-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/dotenvy-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/dotenvy-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/dotenvy-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`allan2/dotenvy`](https://github.com/allan2/dotenvy).

**Original Project:** This port is based on [`allan2/dotenvy`](https://github.com/allan2/dotenvy). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `allan2/dotenvy`

> The text below is reproduced and lightly edited from [`https://github.com/allan2/dotenvy`](https://github.com/allan2/dotenvy). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## dotenvy

[![Crates.io](https://img.shields.io/crates/v/dotenvy.svg)](https://crates.io/crates/dotenvy)
[![msrv
1.74.0](https://img.shields.io/badge/msrv-1.74.0-dea584.svg?logo=rust)](https://github.com/rust-lang/rust/releases/tag/1.74.0)
[![ci](https://github.com/allan2/dotenvy/actions/workflows/ci.yml/badge.svg)](https://github.com/allan2/dotenvy/actions/workflows/ci.yml)
[![docs](https://img.shields.io/docsrs/dotenvy?logo=docs.rs)](https://docs.rs/dotenvy/)

A well-maintained fork of the [dotenv](https://github.com/dotenv-rs/dotenv) crate.

This crate is the suggested alternative for `dotenv` in security advisory [RUSTSEC-2021-0141](https://rustsec.org/advisories/RUSTSEC-2021-0141.html).

## Unpublished version notice

The main branch contains the unpublished 0.16 version which is still subject to change. **The contents of this README are for a version that is not yet published to crates.io**.
The current stable release is [0.15.7](https://docs.rs/dotenvy/0.15.7/dotenvy/).

## Components

1. [`dotenvy`](https://crates.io/crates/dotenvy) - A well-maintained fork of the `dotenv` crate.
2. [`dotenvy_macro`](https://crates.io/crates/dotenvy_macro) - A macro for compile-time _.env_ inspection. This is a fork of `dotenv_codegen`.
3. [`dotenvy-macros`](https://crates.io/crates/dotenvy-macros) - A runtime macro library containing the `load` attribute macro.
4. `dotenvy` binary that loads an env file before executing a specified command.

## What is an environment file?

An _environment file_, or _env file_, is a plain text file consisting of key-value pairs.

_.env_

```sh
HOST=foo
PORT=3000
```

Common names for env files are _.env_, _.env.dev_, _.env.prod_, but any name can be used. The default path for this crate is _.env_.

Variables can span multiple lines and can also be substituted. For an explanation of substituion rules, refer to
the [_.env-substitution_](https://github.com/allan2/dotenvy/blob/HEAD/env-substitution) example file.

## Usage

This library contains two APIs, a non-environment-modifying API and an environment-modifying API.

## Runtime loading

The non-modifying API is recommended for most use cases.

### Non-modifying API

```rs
use dotenvy::{EnvLoader};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let env_map = EnvLoader::new().load()?;
    println!("HOST={}", env_map.var("HOST")?);
    Ok(())
}
```

#### Configuration

```rs
// from a file
let loader1 = EnvLoader::with_path("./.env").sequence(EnvSequence::InputThenEnv);
let loader2 = EnvLoader::new();  // shorthand for loader1

// from a string
let s = "HOST=foo\nPORT=3000";
let str_loader = EnvLoader::with_reader(Cursor::new(s));

// will load from the env file, override exiting values in the program environment
let overriding_loader = EnvLoader::new().sequence(EnvSequence::EnvThenInput);
```

Loader constuction is infallible. When reading from a path, I/O is deferred until the `load` call.
This is to support configurations such as [dev/prod](https://github.com/allan2/dotenvy/blob/HEAD/examples/dev-prod/src/main.rs) and
[optional loading](https://github.com/allan2/dotenvy/blob/HEAD/examples/optional/src/main.rs).

### Modifying API

There are situations where modifying the environment is necessary.
For example, you may be [spawning a child process](https://github.com/allan2/dotenvy/blob/HEAD/examples/modify/src/main.rs) that reads the environment.

dotenvy provides the `load` attribute macro for this purpose. To use it, enable the `macros` feature.

```rs
#[dotenvy::load]
#[tokio::main]
async fn main() {
    println!("HOST={}", std::env::var("HOST").unwrap());
}
```

Because [`set_var`](https://doc.rust-lang.org/stable/std/env/fn.set_var.html) is not thread-safe, the `load` attribute macro modifies the environment before the async runtime is started.
The expansion of this macro is [here](https://github.com/allan2/dotenvy/blob/master/examples/modify-tokio/src/main.rs).

#### Configuration

`load` is configurable. The default configuration expands to:

```rs
#[dotenvy::load(path = "./env", required = true, override_ = false)]
```

For more advanced usage, `EnvLoader::load_and_modify` can be used.

## Compile-time loading

The `dotenv!` macro provided by `dotenvy_macro` crate can be used.

## Minimum Supported Rust Version

We aim to support the latest 8 rustc versions - approximately 1 year. Increasing
MSRV is _not_ considered a semver-breaking change.

## Why does this fork exist?

The original dotenv crate has not been updated since June 26, 2020. Attempts to reach the authors and present maintainer were not successful ([dotenv-rs/dotenv #74](https://github.com/dotenv-rs/dotenv/issues/74)).

This fork intends to serve as the development home for the dotenv implementation in Rust.

## What are the differences from the original?

This repo adds:

- non-modifying API
- configurable `EnvLoader`
- optional loading, ergonomic dev/prod handling
- reader support, such as reading from any reader that is `io::Read`
- more informative `Error` type, containing the file path and variable name
- `load` attribute macro
- multiline support
- more examples and docs

For a full list of changes, refer to the [changelog](https://github.com/allan2/dotenvy/blob/HEAD/CHANGELOG.md).

## Contributing

Contributions are welcome! If you are thinking of contributing, please refer to [CONTRIBUTING.md](https://github.com/allan2/dotenvy/blob/HEAD/CONTRIBUTING.md).

## The legend

Legend has it that the Lost Maintainer will return, merging changes from `dotenvy` into `dotenv` with such thrust that all `Cargo.toml`s will lose one keystroke. Only then shall the Rust dotenv crateverse be united in true harmony.

Until then, this repo dutifully carries on the dotenv torch. It is actively maintained.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:dotenvy-kotlin:0.1.1")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`allan2/dotenvy`](https://github.com/allan2/dotenvy). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the dotenvy authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`allan2/dotenvy`](https://github.com/allan2/dotenvy) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
