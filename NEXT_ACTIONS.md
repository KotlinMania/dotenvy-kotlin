# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/6 (83.3%)
- **Function parity:** 57/67 matched (target 77) — 85.1%
- **Class/type parity:** 9/10 matched (target 21) — 90.0%
- **Combined symbol parity:** 66/77 matched (target 98) — 85.7%
- **Average inline-code cosine:** 0.00 (function body across 3 matched files)
- **Average documentation cosine:** 0.42 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 5
- **Critical Issues:** 5 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. iter

- **Target:** `dotenvy.Iter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3021010.0
- **Functions:** 5/6 matched (target 10)
- **Missing functions:** `new`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/iter.rs` vs expected `iter.rs`
- **Proposed provenance header:** `// port-lint: source iter.rs` (current: `// port-lint: source src/iter.rs`)
- **Lint issues:** 1

### 2. parse

- **Target:** `dotenvy.Parse [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1043410.0
- **Functions:** 27/31 matched (target 35)
- **Missing functions:** `new`, `assert_parsed_string`, `substitute_variable_from_env_variable`, `substitute_variable_env_variable_overrides_dotenv_in_substitution`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Tests:** 20/23 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/parse.rs` vs expected `parse.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/parse.rs` vs expected `parse.rs`
- **Proposed provenance header:** `// port-lint: source parse.rs` (current: `// port-lint: source src/parse.rs`)
- **Proposed provenance header:** `// port-lint: source parse.rs` (current: `// port-lint: source src/parse.rs`)
- **Lint issues:** 2

### 3. lib

- **Target:** `dotenvy.Lib [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11410.0
- **Functions:** 13/14 matched (target 19)
- **Missing functions:** `var`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Lint issues:** 1

### 4. errors

- **Target:** `dotenvy.Errors [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11310.0
- **Functions:** 10/11 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 11)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/errors.rs` vs expected `errors.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/errors.rs` vs expected `errors.rs`
- **Proposed provenance header:** `// port-lint: source errors.rs` (current: `// port-lint: source src/errors.rs`)
- **Proposed provenance header:** `// port-lint: source errors.rs` (current: `// port-lint: source src/errors.rs`)
- **Lint issues:** 2

### 5. find

- **Target:** `dotenvy.Find [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10410.0
- **Functions:** 2/3 matched
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/find.rs` vs expected `find.rs`
- **Proposed provenance header:** `// port-lint: source find.rs` (current: `// port-lint: source src/find.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/dotenvy/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/dotenvy kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
