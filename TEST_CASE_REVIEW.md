# Test Case Review

Individual review of all 54 executed test cases (22 calculator + 9 members + 13 application
core-logic + 10 calculation-utility boundary), assessed for assertion quality, false-positive
risk, and whether each genuinely validates its stated requirement — not merely whether it
executes without error.

## 1. Calculator tests (`CalculatorTest`, 22 cases from `calculator-testdata.json`)

| ID | Scenario | Rule | Assertion quality | Genuinely validates intent? |
|---|---|---|---|---|
| TC01 | Weekly, standard | R1 | Good — independent BigDecimal expected value, compares real UI text | Yes |
| TC02 | Fortnightly, standard | R1 | Good | Yes — correctly fails, exposing BUG-001 |
| TC03 | Monthly, standard | R1 | Good | Yes |
| TC04 | Annual, standard | R1 | Good | Yes |
| TC05 | Lowercase "weekly" | R1/R2 | Good | Only proves lowercase works — see TC06 note below |
| TC06 | "MONTHLY" via dropdown | R2 (as originally labeled) | **Weak — false coverage.** Originally labeled as proving R2; actually only proves the Selenium `Select` helper can match an option regardless of case. The real app always receives the dropdown's fixed lowercase `value` attribute. **Relabeled in this audit** to state this honestly; expected result unchanged. | **No** (for R2) — corrected in this review; genuine R2 coverage now comes from `ApplicationCoreLogicTest` |
| TC07 | Zero amount | R8 | Good | Yes |
| TC08 | Decimal amount | R1/R3 | Good | Yes |
| TC09 | Large amount, comma grouping | R1/R3 | Good | Yes |
| TC10 | Blank | R8 | Good | Yes |
| TC11 | Alphabetic | R8 | Good | Yes |
| TC12 | Negative integer | R8 | Good | Yes — correctly fails, exposing BUG-003 |
| TC13 | Whitespace only | R8 | Good | Yes |
| TC14 | Negative decimal | R8 | Good | Yes — correctly fails, confirms BUG-003 isn't integer-specific |
| TC15 | Trailing decimal point `"5."` | R1 | Good, new | Yes |
| TC16 | Leading plus `"+25"` | R1 | Good, new | Yes |
| TC17 | Scientific notation `"1e3"` | R1/R3 | Good, new | Yes |
| TC18 | Leading zeros `"007"` | R1 | Good, new | Yes |
| TC19 | Comma-formatted `"5,000"` | R8 | Good, new — robustness confirmation | Yes |
| TC20 | Multiple decimal points `"5..5"` | R8 | Good, new — robustness confirmation | Yes |
| TC21 | Hex notation `"0x10"` | R8 (ambiguous) | Good, new — correctly fails, documents Finding F1 | Yes, with documented ambiguity |
| TC22 | `"Infinity"` literal | R8 (ambiguous) | Good, new — correctly fails, documents Finding F2 | Yes, with documented ambiguity |

**Duplicate/redundant scenarios:** none found. TC05 and TC06 looked superficially similar
("case variant" tests) but TC05 (lowercase) is a legitimate baseline while TC06 was
mislabeled rather than duplicated — now corrected.

**Missing negative/boundary scenarios identified and added:** trailing decimal point, leading
plus sign, scientific notation, leading zeros, comma-formatted rejection, multi-decimal
rejection, hex notation, "Infinity" — all 8 added as TC15–TC22.

## 2. Members table tests (`MembersValidationTest`, 9 cases)

| ID / Member | Fields checked | Assertion quality | Notes |
|---|---|---|---|
| `testMembersTableIntegrity` | Row count, uniqueness, every JSON member has a row | Good, new | Closes a real gap: the per-member test only looks up rows by ID, so it could never have detected a wholesale missing/duplicate row on its own |
| M001 | 11 fields (10 original + new member-id cell check) | Good | All pass |
| M002 | 11 fields | Good | Annual Contribution fails — BUG-001 |
| M003 | 11 fields | Good | All pass |
| M004 | 11 fields | Good | Annual Contribution fails — BUG-002 |
| M005 | 11 fields | Good | All pass |
| M006 | 11 fields | Good | All pass (zero/Blocked baseline) |
| M007 | 11 fields | Good | Order Status fails — BUG-004 |
| M008 | 11 fields | Good | Annual Contribution fails — BUG-001 |

**Gap closed in this review:** the displayed text of the `member-id` table cell itself was
not previously asserted — only the presence of a row carrying that `data-member-id`
*attribute* was checked. Added as field #2 in each member's validation. All 8 pass.

**No duplicate/weak scenarios found** in this test — each member exercises a distinct,
meaningful combination of frequency, magnitude, and rounding behavior; this was clearly a
deliberately designed fixture (confirmed by inspection — e.g., M007/M008 are specifically
chosen to sit near the R6/R7 zero-rounding boundary).

## 3. Application core-logic tests (`ApplicationCoreLogicTest`, 13 cases, new in this audit)

| Case | Assertion quality | Outcome |
|---|---|---|
| weekly/Weekly/WEEKLY | Good | All pass — Weekly/WEEKLY pass by coincidence (see `EDGE_CASE_COVERAGE.md`), documented explicitly in the test's own Javadoc to prevent future misreading as "R2 fixed for weekly" |
| fortnightly/Fortnightly/FORTNIGHTLY | Good | All 3 fail — generalizes BUG-001 and BUG-002 |
| monthly/Monthly/MONTHLY | Good | Lowercase passes; Monthly/MONTHLY fail — generalizes BUG-002 beyond the single M004 fixture case |
| annual/Annual/ANNUAL | Good | Lowercase passes; Annual/ANNUAL fail — proves BUG-002 is systemic across all 4 frequencies, not monthly-specific |
| Unsupported frequency (`"biweekly"`) fallback documentation | Good — deliberately does not assert a "correct" value since no rule defines one, only that the observed fallback is stable | Passes (fallback behavior unchanged) |

**False-positive risk check:** none identified. Each assertion compares the real app's own
`annualFor()` return value (called live via `JavascriptExecutor`, not reimplemented) against
`CalculationUtils`' independent BigDecimal calculation — the same pattern used everywhere
else in the suite, so expected values are never derived from the app's own output.

## 4. Calculation utility boundary tests (`CalculationUtilsBoundaryTest`, 10 cases, new in this audit)

| Case | Assertion quality | Outcome |
|---|---|---|
| 9 order-units rounding/status boundaries | Good — each documents its rationale (which rule, which boundary, why) | All pass — confirms `CalculationUtils`' own internal correctness at every requested boundary |
| Fund value HALF_UP tie (`1.0 × 2.005`) | Good | Passes — confirms `CalculationUtils` avoids the classic float-rounding trap via `BigDecimal.valueOf(double)` |

**Scope honesty check:** this class's Javadoc explicitly states it tests the *reference
engine*, not the live application, and explains precisely why the live app cannot be tested
at these specific boundaries with the current fixture and code structure. This prevents the
class from being mistaken for genuine UI-level R6/R7 boundary coverage.

## Cross-cutting findings

- **No evidence that expected values are ever derived from the application's own displayed
  output.** Every expected value traces back to `CalculationUtils` (BigDecimal-based) or to a
  manually independently-verified calculation (confirmed again in this audit via Node.js and
  `jshell` for the new edge cases).
- **No test is marked passed without having actually executed.** Confirmed via direct
  `mvn clean test` execution and cross-checking the surefire/extent report counts against the
  console output (54 run, 16 failed, 0 errors, 0 skipped — matches exactly across three
  independent sources: console, `testng-results.xml`, and the ExtentReports screenshot count).
- **One harmless duplicate file was found and removed:** `src/test/resources/testdata/members.json` was an unused, byte-identical duplicate of the real `data/members.json` that the test actually reads. It was not referenced anywhere in code, so it was deleted during this audit's cleanup pass rather than left to confuse a future maintainer about which file is live.
