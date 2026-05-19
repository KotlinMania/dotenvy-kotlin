# port-lint Proposed Changes

**Generated:** 2026-05-19
**Source:** tmp/dotenvy/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/dotenvy

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/dotenvy/Iter.kt` | `// port-lint: source src/iter.rs` | `// port-lint: source iter.rs` | `iter.rs` | `port-lint provenance header matched only after fallback normalization: 'src/iter.rs' vs expected 'iter.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/dotenvy/Parse.kt` | `// port-lint: source src/parse.rs` | `// port-lint: source parse.rs` | `parse.rs` | `port-lint provenance header matched only after fallback normalization: 'src/parse.rs' vs expected 'parse.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/dotenvy/ParseTest.kt` | `// port-lint: source src/parse.rs` | `// port-lint: source parse.rs` | `parse.rs` | `port-lint provenance header matched only after fallback normalization: 'src/parse.rs' vs expected 'parse.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/dotenvy/Lib.kt` | `// port-lint: source src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'src/lib.rs' vs expected 'lib.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/dotenvy/Errors.kt` | `// port-lint: source src/errors.rs` | `// port-lint: source errors.rs` | `errors.rs` | `port-lint provenance header matched only after fallback normalization: 'src/errors.rs' vs expected 'errors.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/dotenvy/ErrorsTest.kt` | `// port-lint: source src/errors.rs` | `// port-lint: source errors.rs` | `errors.rs` | `port-lint provenance header matched only after fallback normalization: 'src/errors.rs' vs expected 'errors.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/dotenvy/Find.kt` | `// port-lint: source src/find.rs` | `// port-lint: source find.rs` | `find.rs` | `port-lint provenance header matched only after fallback normalization: 'src/find.rs' vs expected 'find.rs'` |
