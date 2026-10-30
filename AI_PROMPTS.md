# AI Prompts Used

This project was built in two AI-assisted sessions:

1. **Initial build** — done in **Google Antigravity**, which produced the Maven/Selenium/
   TestNG framework, the Page Objects, `CalculationUtils`, the calculator and members
   test data, and an initial pass at the bug reports and daily update.
2. **Review, validation, debugging, and submission prep** — done in **Claude Code**, which
   inspected the client application source, independently re-derived the business rules,
   executed the full suite, and reorganized the submission documents.

> **Note on completeness:** The exact prompt-by-prompt history from the Google Antigravity
> session is not available in this workspace (that tool's chat log was not exported alongside
> the project files). The summary below reflects what the resulting code shows it was asked to
> do. If an exported Antigravity chat log becomes available, replace this section with the
> verbatim prompts.

## Antigravity session (inferred from the delivered code)

The existing code, comments, and `HOW_TO_RUN.md` content indicate the build was driven by
requests equivalent to:

- "Build a Java + Selenium + TestNG + Maven automation project for the Contribution Portal
  QA task. Use the Page Object Model, Jackson for JSON, and ExtentReports for HTML reporting."
- "Implement Part 1: a data-driven test for the Annual Contribution Calculator, reading test
  cases from an external JSON file, covering all four frequencies, case variants, zero,
  decimals, large amounts, blank, non-numeric, and negative input."
- "Implement Part 2: a file-to-screen test that reads `data/members.json`, independently
  calculates Annual Contribution, Fund Value, Next Order Units, and Order Status per the
  business rules, and compares them against the Members table using `data-member-id` and
  `data-col` attributes."
- "Calculate all expected values independently using the business rules — do not copy the
  values the app displays."
- "Document any bugs found, with steps to reproduce, expected/actual results, and the rule
  violated."

## Claude Code session (this review)

The main driving prompt for this session (lightly summarized — the full original is long and
itemized by step) was:

> Act as a Senior QA Automation Engineer. Perform a complete technical review, validation,
> debugging, and final submission preparation of an existing QA Automation Practical
> Assessment project (built with Google Antigravity), without modifying the separate
> client-provided application source. Specifically:
> 1. Inspect the existing automation project's structure, dependencies, POM, test data,
>    utilities, and reporting — and separately inspect the client application's HTML/JS/JSON
>    to understand its real selectors and logic. Do not assume; verify against the running app.
> 2. Independently verify all 8 business rules (R1–R8) from the client's README, calculating
>    expected values from the rules, not from what the app displays. Flag any rounding
>    ambiguity rather than silently assuming an answer.
> 3–4. Validate the calculator (Part 1) and Members-table (Part 2) automation against those
>    independently-derived expectations, and identify any missing coverage.
> 5. Execute the full Maven/TestNG suite, fix any genuine framework issues (locators, sync,
>    assertions, JSON parsing, test data, driver config, reporting) — never by changing the
>    application or hard-coding expected values — then re-run to confirm.
> 6. Document only confirmed application defects as formal bug reports (Bug ID, Title,
>    Module, Environment, Steps, Expected, Actual, Rule Reference, Severity, Priority).
> 7. Review the automation framework itself for architecture, readability, reuse,
>    maintainability, waits, and reporting quality, improving only where it adds real value.
> 8. Prepare final submission docs: HOW_TO_RUN.md, AI_PROMPTS.md, README.md, bug reports,
>    and the daily Slack update.
> 9. Give a final, non-fabricated execution summary: tests run/passed/failed/skipped,
>    confirmed defects, framework fixes, and remaining limitations.

### Key follow-up instructions honored throughout

- "Do not confuse the client application folder with the automation project folder."
- "Do not modify the original client application source code."
- "Never hardcode expected UI values; never mark an unexecuted test as passed; never remove a
  failing test to get a clean report."
- "Distinguish application defects from automation script failures, and preserve actual
  application failures as evidence."

## Follow-up Claude Code audit session (this one)

A second, independent audit prompt (lightly summarized — the original was a long, 11-phase
document) was then given:

> Act as a Principal QA Engineer / Independent Test Auditor. Independently verify the
> entire assessment and determine whether the existing test suite provides sufficient
> coverage — do not assume the previous execution report (22 tests, 15 passed, 7 failed, 4
> confirmed defects) is completely correct. Specifically:
> 1. Re-derive every requirement from the README and the real application source into a
>    requirement traceability matrix, independently, not assuming any rule is "fully
>    covered" just because one related test exists.
> 2. Review all 22 existing test cases individually for assertion quality, false-positive
>    risk, duplicate/weak scenarios, and whether each genuinely validates its stated intent.
> 3. Exhaustively analyze calculator edge cases, frequency case-variants, financial
>    precision/rounding boundaries, and Order Status boundary values (0.0000/0.0001/0.0004/
>    0.0005/0.9999/1.0000), using independent BigDecimal calculations and flagging — not
>    silently resolving — any rounding-mode ambiguity.
> 4. Deep-validate the Members table: every member loaded, unique IDs, no missing/duplicate
>    rows, every displayed field independently checked, not just the original 8×10 field set
>    assumed complete without verification.
> 5. Review the entire end-to-end workflow for integration gaps between the calculator, the
>    JSON data, the JavaScript calculations, and the rendered table.
> 6. Audit the automation's own quality: locators, waits, POM, data providers, soft
>    assertions, and specifically whether the same calculation logic is ever accidentally
>    used for both "expected" and "actual" (it must not be).
> 7. Investigate every existing failure and look for additional defects, each with Bug ID,
>    Module, Steps, Expected/Actual, Rule, Severity, Priority, and evidence — but separate
>    confirmed defects from potential risks, and do not create speculative bugs.
> 8. Build a full coverage-gap matrix classifying every scenario as covered/partial/not
>    covered/N-A, based on meaningful scenarios, not just test-method counts.
> 9. Implement only the highest-priority missing test cases, following the existing
>    framework's architecture, using external test data where appropriate, keeping all
>    original failing tests, and never modifying the client's source.
> 10. Re-run the complete suite and report the real before/after numbers — do not fabricate
>     results or change the application to improve the pass percentage.
> 11. Before declaring the project complete, verify compilation, JSON validity, dependencies,
>     reports, test data, git status/diff, that no client source changed, and that no
>     secrets or unnecessary generated files are present.
>
> Deliverables: `FINAL_QA_AUDIT_REPORT.md`, `REQUIREMENT_TRACEABILITY_MATRIX.md`,
> `EDGE_CASE_COVERAGE.md`, `TEST_CASE_REVIEW.md`, `BUG_REPORTS.md`,
> `TEST_EXECUTION_SUMMARY.md`, `HOW_TO_RUN.md`.
>
> Final instruction: do not rush into editing code — show the findings first, then
> implement, then execute everything again and report evidence-based results, thinking like
> a strict client-side QA Lead deciding whether to accept the project.

### What this second pass actually changed

- Added `ApplicationCoreLogicTest` (direct `JavascriptExecutor` calls to the app's real
  `annualFor()` function) to prove R1/R2 across every frequency/case-variant combination —
  something the Calculator UI's dropdown makes architecturally impossible to test directly.
- Added `CalculationUtilsBoundaryTest` for R6/R7 rounding boundaries absent from the client
  fixture, explicitly scoped as reference-engine-level, not UI-level, coverage.
- Added 8 new calculator edge cases (`TC15`–`TC22`), each cross-checked against real
  JS `Number()` and Java `BigDecimal` parsing behavior (via Node.js and `jshell`) before
  being written, rather than guessed.
- Added a table-integrity test and a Member ID cell-text assertion to close two gaps found
  during the per-test-case review.
- Corrected `TC06`'s description/rule-reference (its expected result was unchanged) after
  finding it did not actually exercise R2 as originally labeled.
- Generalized BUG-002 from "one member, one frequency" to a proven, systemic defect, backed
  by the new direct-invocation test's results.
- Re-ran the full suite: 54 tests, 38 passed, 16 failed (up from 22/15/7), with every new
  failure traced to either a known defect (now better-evidenced) or a newly documented,
  clearly-labeled spec ambiguity — none fabricated, none hidden.
