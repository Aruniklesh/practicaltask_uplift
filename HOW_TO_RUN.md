# Contribution Portal - QA Automation Assessment

This repository contains the complete automated test suite for the **Contribution Portal** application using **Java**, **Selenium WebDriver**, **TestNG**, **Maven**, **Jackson**, and **ExtentReports**.

---

## 1. Prerequisites
- **Java JDK**: Version 17 or higher (`java -version`)
- **Apache Maven**: Version 3.8 or higher (`mvn -v`)
- **Google Chrome**: Installed on the machine
- **Local Application Server**: Running on `http://localhost:8000`

---

## 2. Starting the Application Under Test

From the root directory of this project, start a local HTTP server:

```bash
# Using Python:
python -m http.server 8000
```
*or*
```bash
# Using Node:
npx serve -l 8000
```

Verify that the application is accessible at: [http://localhost:8000](http://localhost:8000)

---

## 3. Running the Automation Tests

### A. Run in Headless Mode (Fastest & CI/CD friendly)
```bash
mvn clean test -Dheadless=true
```

### B. Run with Interactive Chrome Browser UI
```bash
mvn clean test
```

### C. Run against a Custom URL
```bash
mvn clean test -Dapp.url=http://localhost:8000
```

---

## 4. Test Suite Contents

The suite runs **54 tests** across 4 TestNG classes (see `src/test/resources/testng.xml`):

| Class | Tests | What it covers |
|---|---|---|
| `tests.CalculatorTest` | 22 | Data-driven calculator validation (Part 1), from `src/test/resources/testdata/calculator-testdata.json` |
| `tests.MembersValidationTest` | 9 | File-to-screen Members table validation (Part 2) + table-integrity check, from `data/members.json` |
| `tests.ApplicationCoreLogicTest` | 13 | Direct JavaScript invocation of the app's real `annualFor()` function across every frequency/case-variant combination (proves R1/R2 beyond what the Calculator's dropdown UI can reach) |
| `tests.CalculationUtilsBoundaryTest` | 10 | R6/R7 rounding-boundary verification of the independent reference engine, at values absent from the client's fixture data |

Expect **38 passed / 16 failed / 0 skipped / 0 errors** — all 16 failures are documented,
reproducible findings (4 confirmed application defects + 2 ambiguous spec findings), not
automation flakiness. See [TEST_EXECUTION_SUMMARY.md](TEST_EXECUTION_SUMMARY.md).

## 5. Test Execution Reports

After running the tests, two comprehensive reports are generated:

1. **Interactive Extent HTML Report**:
   - Location: `target/extent-reports/ExecutionReport.html`
   - Shows interactive dashboards, test step breakdowns, and stack traces.
2. **TestNG Default Reports**:
   - `target/surefire-reports/emailable-report.html`
   - `target/surefire-reports/index.html`

---

## 6. Audit & Submission Documents

| Document | Contents |
|---|---|
| [BUG_REPORTS.md](BUG_REPORTS.md) | Formal bug reports (Bug ID, Module, Steps, Expected/Actual, Rule Reference, Severity, Priority), ambiguous findings F1/F2, and the daily Slack update |
| [REQUIREMENT_TRACEABILITY_MATRIX.md](REQUIREMENT_TRACEABILITY_MATRIX.md) | Every business rule mapped to its test case IDs, coverage level, and any gaps |
| [EDGE_CASE_COVERAGE.md](EDGE_CASE_COVERAGE.md) | Exhaustive edge-case matrix (calculator, frequency, precision, order-status boundaries) |
| [TEST_CASE_REVIEW.md](TEST_CASE_REVIEW.md) | Per-test-case quality review, including corrected/relabeled test metadata |
| [TEST_EXECUTION_SUMMARY.md](TEST_EXECUTION_SUMMARY.md) | Before/after execution numbers for this audit |
| [FINAL_QA_AUDIT_REPORT.md](FINAL_QA_AUDIT_REPORT.md) | Top-level audit narrative and submission-readiness verdict |
| [AI_PROMPTS.md](AI_PROMPTS.md) | AI prompts used across the build and review sessions |
