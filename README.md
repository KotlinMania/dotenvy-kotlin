# dotenvy-kotlin

<p align="center">
  <img src="https://raw.githubusercontent.com/allan2/dotenvy/HEAD/assets/dotenvy.png" alt="dotenvy logo" width="180" />
</p>

<p align="center">
  <strong>A modern, zero-overhead Kotlin Multiplatform port of Rust's <a href="https://github.com/allan2/dotenvy"><code>dotenvy</code></a>.</strong><br/>
  Loads environment variables from <code>.env</code> files across JVM, Android, iOS, macOS, watchOS, tvOS, Linux, Windows, Node.js, and WebAssembly (WasmJS &amp; WasmWASI).
</p>

<p align="center">
  <a href="https://github.com/KotlinMania/dotenvy-kotlin"><img src="https://img.shields.io/badge/GitHub-KotlinMania%2Fdotenvy--kotlin-24292e.svg?logo=github" alt="GitHub" /></a>
  <a href="https://central.sonatype.com/artifact/io.github.kotlinmania/dotenvy-kotlin"><img src="https://img.shields.io/maven-central/v/io.github.kotlinmania/dotenvy-kotlin?color=blue&logo=apache-maven" alt="Maven Central" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4.20-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin 2.4" /></a>
  <a href="https://github.com/KotlinMania/dotenvy-kotlin/actions"><img src="https://img.shields.io/github/actions/workflow/status/KotlinMania/dotenvy-kotlin/ci.yml?branch=main&label=CI&logo=github-actions&logoColor=white" alt="CI" /></a>
  <a href="https://developer.apple.com/swift/"><img src="https://img.shields.io/badge/Swift%20Export-macOS%2015.0-F05138.svg?logo=swift&logoColor=white" alt="Swift Export" /></a>
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License: MIT" /></a>
</p>

---

## ✨ Features

- 🌿 **Faithful Line-by-Line Rust Port** — Exact semantics transliterated from [`allan2/dotenvy`](https://github.com/allan2/dotenvy) with full test parity.
- 🚀 **Full Kotlin Multiplatform Coverage** — JVM, Android, Native (macOS, iOS, tvOS, watchOS, Linux, Windows), Node.js, and WASM (WasmJS & WasmWASI).
- 📦 **First-Class Swift Export** — Directly consume the generated SPM package in Xcode or Swift CLI targets without Obj-C bridges.
- 🔍 **Hierarchical Finder** — Automatically walks up ancestor directories to find `.env` or custom config files.
- ⚡ **Stream & File IO via `km-io`** — Powered by [`io.github.kotlinmania:km-io`](https://github.com/KotlinMania/km-io) with zero external C dependencies.
- 🧬 **Dynamic Variable Substitution** — Resolves `${VAR}` references dynamically across environment values and file definitions.
- 🛡️ **Robust Format Support** — Multi-line strings, weak & strong quotes, escaped characters, trailing/inline comments, and UTF-8 BOM tolerance.
- 🛠️ **CLI Support** — Port of `dotenvy` binary for wrapping commands with injected `.env` state.

---

## 📦 Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.kotlinmania:dotenvy-kotlin:0.1.2")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.kotlinmania:dotenvy-kotlin:0.1.2'
}
```

### Maven (`pom.xml`)

```xml
<dependency>
    <groupId>io.github.kotlinmania</groupId>
    <artifactId>dotenvy-kotlin</artifactId>
    <version>0.1.2</version>
</dependency>
```

### Swift Package Manager

When integrating via Kotlin Multiplatform's Swift Export:

```swift
// Package.swift
dependencies: [
    .package(path: "path/to/Dotenvy")
]
```

---

## 🚀 Quick Start

### 1. Load `.env` into Process Environment

The standard `dotenv()` function searches for `.env` in the current working directory or any of its parent directories, and injects the variables into the system environment:

```kotlin
import io.github.kotlinmania.dotenvy.dotenv
import io.github.kotlinmania.dotenvy.var_

fun main() {
    // Searches up ancestor directories and injects variables into process environment
    dotenv().getOrThrow()

    // Retrieve variables with typed Result returns
    val databaseUrl = var_("DATABASE_URL").getOrThrow()
    val port = var_("PORT").getOrDefault("8080")

    println("Connecting to $databaseUrl on port $port...")
}
```

### 2. Iterate Over Key-Value Pairs

If you want to read variables without modifying the process environment, iterate over parsed pairs:

```kotlin
import io.github.kotlinmania.dotenvy.dotenvIter

fun loadConfiguration(): Map<String, String> {
    val iter = dotenvIter().getOrThrow()
    val config = mutableMapOf<String, String>()

    for (entry in iter) {
        val (key, value) = entry.getOrThrow()
        config[key] = value
    }
    return config
}
```

### 3. Load from a Specific Path or Filename

```kotlin
import io.github.kotlinmania.dotenvy.fromPath
import io.github.kotlinmania.dotenvy.fromPathOverride
import io.github.kotlinmania.dotenvy.fromFilename
import io.github.kotlinmania.io.files.Path

// Load specific path (does not overwrite existing environment variables)
fromPath(Path("config/.env.local")).getOrThrow()

// Force override existing environment variables with file values
fromPathOverride(Path("config/.env.override")).getOrThrow()

// Look for a custom filename walking up ancestor directories
fromFilename(".env.production").getOrThrow()
```

### 4. Custom Hierarchy Search with `Finder`

```kotlin
import io.github.kotlinmania.dotenvy.Finder
import io.github.kotlinmania.io.files.Path

val iter = Finder()
    .filename(Path(".env.staging"))
    .loadIter()
    .getOrThrow()

while (iter.hasNext()) {
    val (key, value) = iter.next().getOrThrow()
    println("$key=$value")
}
```

### 5. In-Memory / Stream Loading with `km-io`

```kotlin
import io.github.kotlinmania.dotenvy.fromRead
import io.github.kotlinmania.dotenvy.fromReadOverride
import io.github.kotlinmania.io.Buffer
import io.github.kotlinmania.io.writeString

val buffer = Buffer().apply {
    writeString("""
        HOST=localhost
        PORT=5432
        DB_USER=admin
        DB_PASS=secret
        DATABASE_URL=postgres://${"$"}{DB_USER}:${"$"}{DB_PASS}@${"$"}{HOST}:${"$"}{PORT}/mydb
    """.trimIndent())
}

// Ingest directly from memory or network stream
fromRead(buffer).getOrThrow()
```

---

## 📝 Format & Substitution Specification

`dotenvy-kotlin` conforms strictly to the `.env` standard:

### Basic Values & Comments
```sh
# Lines beginning with '#' are ignored
KEY=value
ANOTHER_KEY=value with spaces # Inline comments are supported!
```

### Quoting & Multi-line Values
```sh
# Single quotes preserve literal strings exactly
SINGLE_QUOTED='hello\nworld' # Literal \ and n, no escape evaluation

# Double quotes interpret escapes and preserve newlines
DOUBLE_QUOTED="hello\nworld"

# Multi-line strings inside quotes
CERTIFICATE="-----BEGIN CERTIFICATE-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
-----END CERTIFICATE-----"
```

### Variable Substitution
Values can reference variables already defined in the environment or earlier in the file:
```sh
USER=alice
HOME_DIR=/home/${USER}
LOG_FILE=${HOME_DIR}/app.log

# Escape '$' with backslash to prevent substitution:
LITERAL_PRICE=\$100
```

---

## 🌐 Supported Targets

| Platform | Target Identifier | Architecture / Runtime |
| :--- | :--- | :--- |
| **JVM** | `jvm` | Java 21+ bytecode |
| **Android** | `android` | Android API 24+ (AAR) |
| **Apple macOS** | `macosArm64` | Apple Silicon (M1/M2/M3/M4) |
| **Apple iOS** | `iosArm64`, `iosSimulatorArm64` | Device & Simulator |
| **Apple watchOS** | `watchosDeviceArm64`, `watchosSimulatorArm64` | Apple Watch 64-bit |
| **Apple tvOS** | `tvosArm64`, `tvosSimulatorArm64` | Apple TV |
| **Linux** | `linuxX64`, `linuxArm64` | GNU / Linux |
| **Windows** | `mingwX64` | Windows x64 Native |
| **JavaScript** | `js` | Node.js |
| **WebAssembly** | `wasmJs`, `wasmWasi` | WebAssembly Browser & WASI Preview 1 |

---

## 🍏 Swift Export Interoperability

`dotenvy-kotlin` exports native Swift types via Kotlin 2.4 Swift Export:

```swift
import Dotenvy

// Load .env from ancestor hierarchy
let _ = Dotenvy.dotenv()

// Look up values safely
if let host = try? Dotenvy.var_("HOST").get() {
    print("Loaded host: \(host)")
}
```

---

## 🙏 Acknowledgments & Upstream Attribution

`dotenvy-kotlin` is derived from and inspired by the excellent work of the Rust open-source community:

- **[`allan2/dotenvy`](https://github.com/allan2/dotenvy)** — Created and maintained by **Allan Zhang** (`al@ayz.ai`) as a robust, modern fork of the `dotenv` crate.
- **[`dotenv-rs/dotenv`](https://github.com/dotenv-rs/dotenv)** — Original `dotenv` implementation authored by:
  - **Noemi Lapresta** (`noemi.lapresta@gmail.com`)
  - **Craig Hills** (`chills@gmail.com`)
  - **Mike Piccolo** (`mfpiccolo@gmail.com`)
  - **Alice Maz** (`alice@alicemaz.com`)
  - **Sean Griffin** (`sean@seantheprogrammer.com`)
  - **Adam Sharp** (`adam@sharplet.me`)
  - **Arpad Borsos** (`arpad.borsos@googlemail.com`)

All design decisions, syntax rules, and algorithm invariants originate from these authors. We express our deepest gratitude for their foundational contributions to developer tooling.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) to match the upstream `dotenvy` and `dotenv` licensing terms.
