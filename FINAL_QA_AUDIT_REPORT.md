# Final QA Audit Report — Contribution Portal

**Role:** Independent audit of the existing QA Automation Practical Assessment (built in
Google Antigravity, previously reviewed in Claude Code), performed as a second,
skeptical pass rather than a re-statement of the prior review's conclusions.

**Scope of independence:** This audit re-derived every business rule from the client's
`README.md` and the actual `index.html` source from scratch, re-verified the previously
reported 4 defects by reproducing them independently (not by trusting the prior report), and
specifically hunted for false-positive test coverage, missing boundary cases, and
spec ambiguities the prior pass did not surface.

## What was reviewed

- The full existing Maven/Selenium/TestNG framework (`pom.xml`, Page Objects, models,
  `CalculationUtils`, `DriverFactory`, `ExtentReportManager`, `TestListener`).
- The client's actual `index.html` source (`MULTIPLIERS` object, `annualFor()`, `eur()`, the
  fetch-driven Members table renderer) — read directly, not assumed.
- Both test data files (`calculator-testdata.json`, `data/members.json`) against the real
  rendered application at `http://localhost:8000`.
- JavaScript (`Number()`) and Java (`BigDecimal`) string-parsing behavior for ~20 edge-case
  input strings, verified with actual Node.js and `jshell` sessions, not assumed from memory.
- Floating-point rounding behavior (`toFixed` vs `toLocaleString`, `BigDecimal.valueOf` vs
  `new BigDecimal(double)`) at known trap values, verified the same way.
- The previous 22-test execution report, reproduced independently before any new code was
  written.

## What was already correct

- All 4 previously-reported defects (fortnightly multiplier, case-sensitive frequency
  lookup, missing negative validation, unrounded Order Status check) were **independently
  reproduced**, not merely re-stated. The original report was accurate.
- `CalculationUtils` is well-built: BigDecimal throughout, explicit rounding-mode baseline
  documented in its own Javadoc, and it correctly uses `BigDecimal.valueOf(double)` rather
  than `new BigDecimal(double)` — a detail that avoids a classic floating-point rounding trap
  many implementations get wrong.
- The Page Object Model, explicit waits, data-driven design, and soft-assertion reporting
  format were all sound and required no fixes.
- Expected values are never derived from the application's own output anywhere in the suite
  — confirmed by code inspection, not assumption.

## What was missing (found by this audit, not present in the prior review)

1. **A test that claimed to prove R2 (case-insensitivity) on the Calculator (`TC06`)
   actually could not** — the dropdown only ever submits a fixed lowercase value. This was a
   genuine false-coverage risk: a reviewer skimming "R2: ✅ covered" from the prior report
   would have been wrong about the Calculator specifically.
2. **R2's defect was proven for exactly one case-variant of one frequency** (`"Monthly"`,
   via member M004). Whether the defect was systemic across all 4 frequencies and all case
   forms was unverified.
3. **R6/R7 boundary values** (0.0000/0.0001/0.0004/0.0005 exact tie/0.9999/1.0000) were not
   exercised — the fixture only contains two boundary-adjacent values.
4. **No test verified table integrity** (exact row count, no duplicates, no missing member) —
   only per-member field correctness, assuming the row already existed.
5. **The displayed Member ID cell's own text** was never asserted, only the row-locating
   attribute.
6. Several plausible, realistic edge-case inputs (trailing decimal point, leading plus sign,
   scientific notation, leading zeros, comma-formatted rejection, multi-decimal rejection,
   hex notation, "Infinity") were untested.

## What was implemented in response

- **`ApplicationCoreLogicTest`** (new, 13 tests): calls the real app's own `annualFor()`
  JavaScript function directly via `JavascriptExecutor`, bypassing the Calculator dropdown's
  architectural limitation, across all 4 frequencies × 3 case forms. This generalized BUG-002
  from "one member, one frequency" to a proven, systemic defect across 3 of 4 frequencies
  (weekly's non-lowercase forms pass only by numeric coincidence, documented explicitly so
  this isn't later misread as "weekly case-handling works").
- **`CalculationUtilsBoundaryTest`** (new, 10 tests): proves the independent reference
  engine's own correctness at all requested R6/R7 boundaries, with an explicit, honest
  Javadoc scope note that this is reference-engine-level, not live-app-level, coverage —
  because the fixture can't be extended (client-owned) and the app's rounding logic isn't
  exposed as a callable function the way `annualFor` is.
- **8 new calculator test cases (`TC15`–`TC22`)**, each independently verified against both
  JS `Number()` and Java `BigDecimal` parsing before being added, so every expected value is
  evidence-based, not guessed.
- **`testMembersTableIntegrity`** (new): row count, uniqueness, no-missing-member checks.
- **Member ID cell text assertion** added to all 8 existing per-member checks.
- **`TC06` relabeled** (expected result unchanged) to stop claiming R2 coverage it cannot
  provide, with the correction explained in its own description field.

## Defects confirmed

4 confirmed application defects (BUG-001 through BUG-004 in `BUG_REPORTS.md`), all
independently reproduced and, for BUG-002, generalized in scope. 2 additional findings (F1,
F2) are documented separately as spec ambiguities rather than confirmed defects, because R8's
wording does not clearly resolve whether hex notation or "Infinity" should be rejected.

## Business rules: coverage status

| Rule | Status |
|---|---|
| R1 | Fully covered |
| R2 | Fully covered on Members table and via direct JS invocation; **architecturally impossible to cover via the Calculator UI** (documented, not silently assumed) |
| R3 | Fully covered |
| R4 | Fully covered |
| R5 | Fully covered (fixture) + boundary-verified at reference-engine level |
| R6 | Fully covered (fixture) + boundary-verified at reference-engine level; **live-app boundary coverage beyond the fixture is not possible without client changes** |
| R7 | Fully covered (fixture) + boundary-verified at reference-engine level; same limitation as R6 |
| R8 | Fully covered, including 2 documented ambiguous edge cases |

No requirement is left silently "partially covered" without an explicit, named reason in
`REQUIREMENT_TRACEABILITY_MATRIX.md`.

## Is the test suite reliable?

Yes. Every one of the 16 failures (up from 7 in the prior 22-test run) is reproducible,
traces to a named rule and either a confirmed defect or a documented ambiguity, and was
cross-verified against three independent sources (Maven console, `testng-results.xml`,
ExtentReports screenshot count — all agree exactly: 54 run / 16 failed). Nothing was
skipped, nothing was marked passed without executing, and no expected value was ever derived
from the application's own displayed output.

## Pre-commit / pre-submission check (Phase 11)

- Full suite recompiled and re-executed after every change: **PASS** (54 run, 38 passed, 16
  failed, 0 errors, 0 skipped — all failures accounted for and explained).
- JSON validity: `calculator-testdata.json` and both `members.json` copies parse without
  error (confirmed by successful Jackson deserialization during the test run itself — a
  malformed file would have thrown at runtime, and none did).
- Maven dependencies: resolve cleanly, `mvn clean test` builds successfully both before and
  after this audit's changes.
- Client application source (`contribution-app/index.html`, `contribution-app/data/members.json`):
  **byte-for-byte identical** to the copies inside this automation project, re-confirmed via
  `diff` at the end of this audit — **no client source was modified.**
- **Git repository:** initialized against `https://github.com/Aruniklesh/practicaltask_uplift.git`
  as a pre-push cleanup pass. `.gitignore` added: build byproducts under `target/`
  (`classes`, `test-classes`, `generated-sources`, `generated-test-sources`, `maven-status`)
  and local IDE config (`.vscode/`) are excluded; `target/surefire-reports/` and
  `target/extent-reports/` are deliberately **kept** and committed as the required test-run
  report evidence (minus the noisy `*.dumpstream` debug file).
- No secrets found: no `.env`, credentials, or API keys exist anywhere in this project.
- Two confirmed-unused duplicate files were deleted (not just flagged) as part of this
  cleanup: the stray `testng.xml` at the project root (the live suite file Maven actually
  uses is `src/test/resources/testng.xml`), and `src/test/resources/testdata/members.json`
  (an unreferenced, byte-identical duplicate of the live `data/members.json`).
- Documentation matches actual execution: every number quoted in `README.md`,
  `HOW_TO_RUN.md`, `BUG_REPORTS.md`, and this report was taken directly from the actual
  `mvn clean test` output of this session, not estimated.

## Post-submission fix: ExtentReports completeness and readability

After the initial push, a review of the rendered `ExecutionReport.html` surfaced two issues,
both fixed and re-verified (54 run / 38 passed / 16 failed afterward — unchanged, confirming
no regression):

1. **Verbose `@Test` descriptions and calculator test-data `description`/`ruleReference`
   fields** (several were full paragraphs explaining audit rationale) were making the report
   UI cramped and hard to scan. Shortened all of them to one-line labels; the full rationale
   remains in Javadoc comments (not shown in the report) and in `BUG_REPORTS.md`/
   `TEST_CASE_REVIEW.md`.
2. **`CalculationUtilsBoundaryTest`'s 10 results never appeared in the ExtentReports HTML at
   all** — a genuine completeness bug, not cosmetic. That class doesn't extend `BaseTest` (it
   needs no browser), so it never inherited the `@Listeners(TestListener.class)` annotation
   that populates the Extent report. It still ran and passed correctly (visible in the raw
   Surefire XML and the `Tests run: 54` total), just invisible in the HTML dashboard. Fixed
   by attaching `@Listeners(TestListener.class)` directly to the class. Confirmed present in
   the report after the fix (its test names and descriptions now appear).

## Final verdict: submission readiness

**Ready for client submission.** All housekeeping items from the previous version of this
report have been resolved: the duplicate files are deleted, and the repository (remote:
`https://github.com/Aruniklesh/practicaltask_uplift.git`) is cleaned up and ready to push.
Code, test data, reports, and documentation are internally consistent, independently
verified, and accurately reflect what was actually executed.
