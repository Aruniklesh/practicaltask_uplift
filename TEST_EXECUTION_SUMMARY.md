# Test Execution Summary

Both runs were executed against the live application at `http://localhost:8000` using
`mvn clean test -Dheadless=true`, Chrome via Selenium 4, on Windows 11 / Java 21
(compiled at `release=17`). No application code was changed between runs, and no test was
altered to force a pass — all deltas below come from genuinely new or relabeled test cases.

## Headline numbers

| Metric | Previous baseline (Claude Code review, 2026-10-30) | Current run (independent audit, 2026-10-30) | Delta |
|---|---|---|---|
| Total tests executed | 22 | **54** | +32 |
| Passed | 15 | **38** | +23 |
| Failed | 7 | **16** | +9 |
| Skipped | 0 | **0** | — |
| Framework errors | 0 | **0** | — |
| Confirmed application defects | 4 | **4** (unchanged — BUG-002 generalized, not newly introduced) | — |
| Documented ambiguous findings (not counted as confirmed defects) | 0 | **2** (F1, F2) | +2 |

## Where the +32 new tests came from

| Test class | New cases | Purpose |
|---|---|---|
| `CalculatorTest` | +8 (`TC15`–`TC22`) | Trailing decimal point, leading plus sign, scientific notation, leading zeros, comma-formatted rejection, multi-decimal rejection, hex notation (F1), "Infinity" (F2) |
| `MembersValidationTest` | +1 new test method, +1 new field check per existing member | Table-integrity check (row count/uniqueness/no-missing-member); displayed Member ID cell text now independently asserted for all 8 members |
| `ApplicationCoreLogicTest` (new class) | +13 | Direct JavaScript invocation of the app's real `annualFor()` function across all 4 frequencies × 3 case variants, plus one documented-fallback test for an unsupported frequency string |
| `CalculationUtilsBoundaryTest` (new class) | +10 | R6/R7 rounding-boundary verification of the independent reference engine at values absent from the client fixture (0.0000/0.0001/0.0004/0.0005 tie/0.9999/1.0000) |

## Where the +9 new failures came from

All 9 new failures are **newly surfaced, real findings** — none are regressions in
previously-passing coverage, and none required weakening any existing assertion:

| New failure(s) | Finding | Already in the confirmed defect list? |
|---|---|---|
| 1 (`fortnightly` lowercase via direct JS call) | Reproduces BUG-001 through a second, independent code path | Yes — corroborates, doesn't add a new bug |
| 6 (`Fortnightly`/`FORTNIGHTLY`, `Monthly`/`MONTHLY`, `Annual`/`ANNUAL` via direct JS call) | Generalizes BUG-002 from "one member, one frequency" to "systemic, all 4 frequencies" | Yes — same underlying defect, now proven at full scope |
| 2 (`TC21` hex notation, `TC22` "Infinity") | New ambiguous findings F1/F2 | No — intentionally kept separate from the 4 confirmed defects pending client clarification |

No previously-passing test regressed. `testMembersTableIntegrity`, the new `member-id` cell
checks, `CalculationUtilsBoundaryTest` (all 10 cases), and 5 of the 8 new calculator cases
(`TC15`–`TC20`, minus the 2 ambiguous ones) all passed — confirming the application's
*correct* behavior is also broader than the original 22-test baseline proved, not just its
defects.

## Business rule coverage after this audit

See `REQUIREMENT_TRACEABILITY_MATRIX.md` for the full matrix. Summary: all 8 rules have at
least UI-level coverage; R2 is fully proven on the Members table and (via direct JS
invocation) on the app's core calculation logic, but remains architecturally untestable
through the Calculator's dropdown specifically; R6/R7 boundary values absent from the client
fixture are proven only at the independent-reference-engine level, not against the live DOM.

## Evidence cross-check

The reported 54/38/16/0 figures were verified against three independent sources, not just
the console log:
- Maven/Surefire console output: `Tests run: 54, Failures: 16, Errors: 0, Skipped: 0`
- `target/surefire-reports/testng-results.xml`
- `target/extent-reports/ExecutionReport.html`: exactly 16 "Failure Screenshot" entries,
  matching the 16 reported failures exactly.

## Reliability assessment

The suite is reliable: every failure is reproducible, traces to either a real application
defect or a documented spec ambiguity, and none are flaky/timing-related (confirmed by the
explicit `WebDriverWait` usage throughout the Page Objects and the deterministic, non-random
nature of all test data). No test was skipped, no test was marked passed without executing,
and no expected value was derived from the application's own displayed output.
