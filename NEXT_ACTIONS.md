# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/6 (83.3%)
- **Function parity:** 59/67 matched (target 79) — 88.1%
- **Class/type parity:** 9/10 matched (target 21) — 90.0%
- **Combined symbol parity:** 68/77 matched (target 100) — 88.3%
- **Average inline-code cosine:** 0.59 (function body across 5 matched files)
- **Average documentation cosine:** 0.44 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. iter

- **Target:** `dotenvy.Iter`
- **Similarity:** 0.50
- **Dependents:** 3
- **Priority Score:** 3021005.0
- **Functions:** 5/6 matched (target 10)
- **Missing functions:** `new`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `Item`

### 2. parse

- **Target:** `dotenvy.Parse`
- **Similarity:** 0.61
- **Dependents:** 1
- **Priority Score:** 1023403.9
- **Functions:** 29/31 matched (target 37)
- **Missing functions:** `new`, `assert_parsed_string`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Tests:** 22/23 matched

### 3. lib

- **Target:** `dotenvy.Lib`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 11403.9
- **Functions:** 13/14 matched (target 19)
- **Missing functions:** `var`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 4. errors

- **Target:** `dotenvy.Errors`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 11303.7
- **Functions:** 10/11 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 11)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 5. find

- **Target:** `dotenvy.Find`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 10404.2
- **Functions:** 2/3 matched
- **Missing functions:** `new`
- **Types:** 1/1 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

