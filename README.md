# Contribution Portal — QA Automation Project

A Java/Selenium/TestNG automation solution for the Contribution Portal QA practical
assessment. It automates both required parts of the brief against the real application
(no mocked UI, no hardcoded expected values) and reports results with an interactive HTML
report plus structured console output.

## What this project covers

| Part | Scope | Test class | Test data |
|---|---|---|---|
| 1 — Calculator | Data-driven test of the Annual Contribution Calculator (all 4 frequencies, case variants, zero, decimal, large, blank, non-numeric, negative, whitespace, plus 8 further edge cases added in the follow-up audit) | `tests.CalculatorTest` | `src/test/resources/testdata/calculator-testdata.json` (22 cases) |
| 2 — Members table | File-to-screen check: reads `data/members.json`, independently calculates expected values, compares every field for all 8 members against the on-screen table, plus a table-integrity check | `tests.MembersValidationTest` | `data/members.json` |
| Audit addition — Core logic | Direct JS invocation of the app's real `annualFor()` function across every frequency/case-variant combination, to prove R1/R2 beyond what the Calculator's dropdown UI can reach | `tests.ApplicationCoreLogicTest` | inline |
| Audit addition — Boundary verification | R6/R7 rounding-boundary checks of the independent reference engine at values absent from the client fixture | `tests.CalculationUtilsBoundaryTest` | inline |

Expected values for both parts are computed independently in `utils/CalculationUtils.java`
using `java.math.BigDecimal`, directly from the business rules below — never from what the
UI happens to display.

## Architecture

- **Build:** Maven (`pom.xml`), Java 17 target, JDK 21 used locally.
- **Driver management:** `utils/DriverFactory.java` — thread-local Selenium `ChromeDriver`,
  WebDriverManager-assisted, with a `-Dheadless=true` switch.
- **Page Object Model:** `pages/CalculatorPage.java`, `pages/MembersTablePage.java` —
  locators matched to the real selectors in the app (`#amount`, `#frequency`, `#calculate`,
  `#annual-result`, `#error-msg`, and `tr[data-member-id]` / `td[data-col]` in the members
  table).
- **Models:** `models/CalculatorTestCase.java`, `models/Member.java` — JSON-backed POJOs
  (Jackson), `BigDecimal` fields for exact precision.
- **Business logic (source of truth for expected values):** `utils/CalculationUtils.java` —
  implements R1–R8, documents a rounding-mode ambiguity in R3/R5/R6 and defaults to
  `HALF_UP`.
- **Reporting:** `utils/ExtentReportManager.java` + `utils/TestListener.java` — ExtentReports
  HTML report with failure screenshots, alongside the standard Surefire/TestNG reports.
- **Test data:** `testdata/calculator-testdata.json` holds each calculator scenario's input,
  expected outcome type (`SUCCESS`/`ERROR`), and the rule it exercises.

## Business rules (source of truth, from the client's original brief)

| # | Rule |
|---|------|
| R1 | Annual contribution = contribution amount × multiplier. Multipliers: weekly 52, fortnightly 26, monthly 12, annual 1. |
| R2 | Payment frequency must be treated the same whatever its case ("Monthly" = "monthly"). |
| R3 | Money values show with a € sign and exactly 2 decimal places, e.g. €1,300.00. |
| R4 | Units Held shows exactly 7 decimal places. |
| R5 | Fund Value = Units Held × Unit Price, rounded to 2 decimal places. |
| R6 | Orders can only be sent with up to 4 decimal places. Next Order Units = order quantity rounded to 4 decimal places. |
| R7 | Order Status is **Blocked** if Next Order Units (after rounding to 4 decimal places) is 0, otherwise **Ready**. |
| R8 | Blank/non-numeric amount → "Enter a valid amount". Negative amount → "Amount cannot be negative". Zero is allowed. |

## Result of the latest run

54 tests executed, 38 passed, 16 failed, 0 framework errors, 0 skipped. All 16 failures trace
to 4 confirmed application defects (not automation issues) and 2 documented spec-ambiguity
findings — see [BUG_REPORTS.md](BUG_REPORTS.md) for full details:

1. Fortnightly multiplier is 24 instead of 26 (R1).
2. Case-sensitive frequency lookup silently falls back to the wrong multiplier — proven
   systemic across all 4 frequencies, not just one member (R2).
3. Calculator never validates negative amounts (R8).
4. Order Status is computed from the unrounded order quantity instead of the rounded one (R7).
5. (Ambiguous finding, not a confirmed defect) Hex-notation amounts bypass validation (R8).
6. (Ambiguous finding, not a confirmed defect) The literal "Infinity" bypasses validation (R8).

See [FINAL_QA_AUDIT_REPORT.md](FINAL_QA_AUDIT_REPORT.md) for the full independent audit,
[REQUIREMENT_TRACEABILITY_MATRIX.md](REQUIREMENT_TRACEABILITY_MATRIX.md) for rule-by-rule
coverage, and [TEST_EXECUTION_SUMMARY.md](TEST_EXECUTION_SUMMARY.md) for the before/after
numbers.

## Running the tests and viewing the report

1. Start the application (from the project root, in a separate terminal):
   ```bash
   python -m http.server 8000
   ```
   Confirm it's up at [http://localhost:8000](http://localhost:8000).
2. Run the suite (headless is faster; drop the flag to watch Chrome run the tests):
   ```bash
   mvn clean test -Dheadless=true
   ```
   This always regenerates the reports below, overwriting the previous run's output.
3. View the report — three formats are generated, pick whichever you need:

   | Report | Path | Open with |
   |---|---|---|
   | **Plain-text report** (what prints to console, saved to disk) | `target/surefire-reports/TestSuite.txt` | Any text editor — one line per test, full stack trace for every failure |
   | **TestNG HTML report** | `target/surefire-reports/index.html` | Double-click it, or open with a browser — pass/fail counts per test, grouped by class |
   | **TestNG emailable summary** | `target/surefire-reports/emailable-report.html` | Browser — single-page summary, good for pasting into an email/PR |
   | **Interactive ExtentReports** | `target/extent-reports/ExecutionReport.html` | Browser — dashboards, per-test step logs, and a screenshot attached to every failure |

   On Windows, open any HTML report directly from a terminal with:
   ```bash
   start target/surefire-reports/index.html
   start target/extent-reports/ExecutionReport.html
   ```

See [HOW_TO_RUN.md](HOW_TO_RUN.md) for full prerequisites and additional run options (custom
URL, etc.).

## Other documents

- [HOW_TO_RUN.md](HOW_TO_RUN.md) — prerequisites, exact run commands, report locations.
- [BUG_REPORTS.md](BUG_REPORTS.md) — formal bug reports, ambiguous findings, and the daily Slack update.
- [REQUIREMENT_TRACEABILITY_MATRIX.md](REQUIREMENT_TRACEABILITY_MATRIX.md) — every rule mapped to its test IDs, coverage, and gaps.
- [EDGE_CASE_COVERAGE.md](EDGE_CASE_COVERAGE.md) — exhaustive edge-case matrix.
- [TEST_CASE_REVIEW.md](TEST_CASE_REVIEW.md) — per-test-case quality review.
- [TEST_EXECUTION_SUMMARY.md](TEST_EXECUTION_SUMMARY.md) — before/after execution numbers.
- [FINAL_QA_AUDIT_REPORT.md](FINAL_QA_AUDIT_REPORT.md) — top-level audit narrative and submission-readiness verdict.
- [AI_PROMPTS.md](AI_PROMPTS.md) — AI prompts used across the build and review sessions.
