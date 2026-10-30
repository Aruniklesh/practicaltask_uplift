# Bug Reports — Contribution Portal

All expected values below were calculated independently from the business rules in the
client-provided `README.md` (R1–R8), not from what the application displays. Each bug was
confirmed by automated test execution (see `target/surefire-reports/` and
`target/extent-reports/ExecutionReport.html`). Initial confirmation: 2026-10-30, 22 tests
(15 passed / 7 failed). Re-confirmed and generalized in an independent follow-up audit the
same day after expanding the suite to 54 tests (38 passed / 16 failed), run against
`http://localhost:8000` using Chrome via Selenium/TestNG.

| Bug ID | Title | Severity | Priority |
|---|---|---|---|
| BUG-001 | Fortnightly multiplier is 24 instead of 26 | High | P1 |
| BUG-002 | Case-sensitive frequency lookup falls back to the wrong multiplier — confirmed systemic across all 4 frequencies, not just "Monthly" | High | P1 |
| BUG-003 | Calculator does not reject negative amounts | Medium | P2 |
| BUG-004 | Order Status uses the unrounded value instead of the rounded one | High | P1 |

Two additional **documented ambiguous findings** (not counted as confirmed defects — see
the "Judgment-call findings" section below) were surfaced by deeper edge-case testing in the
follow-up audit: hex-notation amounts (F1) and the literal string "Infinity" (F2) are
accepted by the app's `Number()`-based validation even though a stricter numeric parser
(Java's `BigDecimal`) rejects them.

---

## BUG-001: Fortnightly multiplier is 24 instead of 26

- **Module:** Annual Contribution Calculator & Members Table (shared `MULTIPLIERS` constant in `index.html`)
- **Environment:** `http://localhost:8000`, Google Chrome, Windows 11
- **Steps to Reproduce:**
  1. Open `http://localhost:8000`.
  2. In the calculator, enter `50` as the amount, select **Fortnightly**, click **Calculate**.
  3. Separately, inspect members `M002` (Liam Walsh, €50, fortnightly) and `M008` (Darragh Quinn, €25, fortnightly) in the Members table.
- **Expected Result:** Annual Contribution = amount × 26.
  - Calculator: `Annual contribution: €1,300.00`
  - M002 Annual Contribution: `€1,300.00`
  - M008 Annual Contribution: `€650.00`
- **Actual Result:** The app's `MULTIPLIERS` object defines `fortnightly: 24`.
  - Calculator shows: `Annual contribution: €1,200.00`
  - M002 shows: `€1,200.00`
  - M008 shows: `€600.00`
- **Business Rule Reference:** R1 — "The multipliers are weekly 52, fortnightly 26, monthly 12, annual 1."
- **Severity:** High — every fortnightly contributor's annual figure is wrong by a fixed ~7.7% understatement.
- **Priority:** P1 — core calculation rule, affects multiple members and the primary calculator feature.
- **Automated evidence:** `CalculatorTest.testAnnualContributionCalculator[TC02_FortnightlyPayment]`, `MembersValidationTest.testMemberRowCalculations[M002]`, `[M008]`.

---

## BUG-002: Case-sensitive frequency lookup falls back to the wrong multiplier — systemic, not member-specific

- **Module:** Members Table and Calculator (shared `annualFor()`/`MULTIPLIERS` lookup in `index.html`)
- **Environment:** `http://localhost:8000`, Google Chrome, Windows 11
- **Steps to Reproduce:**
  1. Open `http://localhost:8000`.
  2. Locate member `M004` (Conor Doyle), whose record has `"frequency": "Monthly"` (capital M) and `contributionAmount: 150`.
  3. Read the Annual Contribution cell for M004.
  4. (Follow-up audit) Directly invoke the app's own `annualFor()` function in the browser console / via `JavascriptExecutor` with every case variant of all four frequencies: `annualFor(100, 'Weekly')`, `annualFor(100, 'WEEKLY')`, `annualFor(100, 'Fortnightly')`, `annualFor(100, 'FORTNIGHTLY')`, `annualFor(100, 'Monthly')`, `annualFor(100, 'MONTHLY')`, `annualFor(100, 'Annual')`, `annualFor(100, 'ANNUAL')`.
- **Expected Result:** Frequency matching must be case-insensitive (Rule R2) for all four frequencies. `annualFor(100, X)` must return the same value regardless of X's case.
- **Actual Result:** `MULTIPLIERS[frequency]` is a case-sensitive plain-object lookup keyed only by exact lowercase strings, with a `|| 52` fallback. Confirmed via direct invocation of the real app's `annualFor()` function (test class `ApplicationCoreLogicTest`, not a reimplementation):
  | Input | Returned | Correct (per R1/R2) |
  |---|---|---|
  | `Monthly` / `MONTHLY` | 5200 (100 × 52) | 1200 (100 × 12) |
  | `Fortnightly` / `FORTNIGHTLY` | 5200 (100 × 52) | 2600 (100 × 26) |
  | `Annual` / `ANNUAL` | 5200 (100 × 52) | 100 (100 × 1) |
  | `Weekly` / `WEEKLY` | 5200 (100 × 52) | 5200 (100 × 52) — **passes only by coincidence**, since the fallback value happens to equal weekly's own correct multiplier |

  This proves the defect is **systemic across all four frequencies**, not limited to the
  "Monthly" string that the original fixture happened to expose via member M004. Only
  weekly is immune, and only by coincidence (the `|| 52` fallback equals weekly's correct
  value) — it does not mean weekly's case-handling is actually implemented correctly.
- **Business Rule Reference:** R2 — "The payment frequency in the data must be treated the same whatever its case."
- **Severity:** High — for 3 of 4 frequencies, any non-lowercase-exact input produces a wildly incorrect result (up to 52× overstatement for annual contributions) with no indication anything is wrong.
- **Priority:** P1 — directly violates an explicitly named business rule, is systemic rather than an isolated data quirk, and silently corrupts a financial figure.
- **Automated evidence:** `MembersValidationTest.testMemberRowCalculations[M004]`; `ApplicationCoreLogicTest.testAnnualForDirectInvocation` (7 of 12 case-variant combinations fail, all for non-lowercase-exact frequency strings).

---

## BUG-003: Calculator does not reject negative amounts

- **Module:** Annual Contribution Calculator (`index.html`, `calculate` click handler)
- **Environment:** `http://localhost:8000`, Google Chrome, Windows 11
- **Steps to Reproduce:**
  1. Open `http://localhost:8000`.
  2. Enter `-10` as the amount, select **Weekly**, click **Calculate**.
- **Expected Result:** Per R8, a negative amount must show the error `Amount cannot be negative`, and no annual result should be calculated.
- **Actual Result:** The click handler only checks for blank input and `isNaN(amount)`. There is no negative check, so it proceeds to calculate and displays `Annual contribution: €-520.00`.
- **Business Rule Reference:** R8 — "A negative amount must show the error 'Amount cannot be negative'."
- **Severity:** Medium — no data integrity risk (calculator is display-only), but it silently produces a nonsensical negative currency value instead of the required validation message.
- **Priority:** P2 — an explicit, named rule is violated, but the input is an edge case unlikely in normal use.
- **Automated evidence:** `CalculatorTest.testAnnualContributionCalculator[TC12_NegativeAmount]`, `[TC14_NegativeDecimalAmount]`.

---

## BUG-004: Order Status uses the unrounded value instead of the rounded one

- **Module:** Members Table (order status logic in `index.html`)
- **Environment:** `http://localhost:8000`, Google Chrome, Windows 11
- **Steps to Reproduce:**
  1. Open `http://localhost:8000`.
  2. Locate member `M007` (Ciara Nolan), whose record has `"nextOrderUnits": 0.00004`.
  3. Read the Next Order Units and Order Status cells for M007.
- **Expected Result:** Per R6, Next Order Units is rounded to 4 decimal places: `0.00004` → `0.0000`. Per R7, Order Status must be **Blocked** when the rounded value is 0.
- **Actual Result:** Next Order Units correctly displays `0.0000` (it is rounded for display). However, the status line computes `status = m.nextOrderUnits > 0 ? 'Ready' : 'Blocked'` using the **raw, unrounded** value (`0.00004 > 0` is `true`), so Order Status displays **Ready** — inconsistent with the `0.0000` shown right next to it.
- **Business Rule Reference:** R7 — "Order Status must be Blocked if Next Order Units (after rounding to 4 decimal places) is 0."
- **Severity:** High — an order that should be blocked is marked Ready, which is a financial/operational risk in a real dealing system, and the row is internally inconsistent (0.0000 units shown as "Ready" to order).
- **Priority:** P1 — core order-control rule with a direct real-world consequence.
- **Automated evidence:** `MembersValidationTest.testMemberRowCalculations[M007]`.

---

## Judgment-call findings (confirmed by a real failing test, but NOT counted as confirmed defects)

These two findings are real, reproducible test failures — not speculation — but are kept
separate from the confirmed-defects list above because R8's wording ("non-numeric amount")
is genuinely ambiguous about whether they should be rejected. A strict QA Lead should see
these surfaced, but they should not be reported to the client with the same confidence as
BUG-001 through BUG-004.

### Finding F1: Hexadecimal notation amount bypasses "non-numeric" validation

- **Module:** Annual Contribution Calculator (`index.html`, `calculate` click handler)
- **Steps to Reproduce:** Enter `0x10` as the amount, select any frequency, click Calculate.
- **Observed:** JavaScript's `Number("0x10")` evaluates to `16` (not `NaN`), so the app's
  `isNaN(amount)` check does not reject it — it silently treats a hexadecimal literal as a
  valid monetary amount and calculates `16 × multiplier`. Java's `BigDecimal` constructor,
  by contrast, does not support hex notation and throws `NumberFormatException`.
- **Why this is ambiguous, not a clear-cut bug:** R8 only says "non-numeric amount" must be
  rejected. Whether a hex literal counts as "numeric" is genuinely not defined by the spec —
  JavaScript's own type system says it is a valid number.
- **Recommendation:** Clarify with the client whether non-decimal numeric literal forms
  (hex, octal, scientific notation) should be accepted for a currency field. If not, the fix
  would be a stricter regex-based validation (e.g. `^\d*\.?\d*$`) instead of relying on
  `Number()`/`isNaN()`.
- **Automated evidence:** `CalculatorTest.testAnnualContributionCalculator[TC21_HexNotationAmount_AmbiguousEdgeCase]` — fails as documented (expected "Enter a valid amount", actual: no error, calculation proceeds).

### Finding F2: The literal string "Infinity" bypasses "non-numeric" validation

- **Module:** Annual Contribution Calculator (`index.html`, `calculate` click handler)
- **Steps to Reproduce:** Enter `Infinity` as the amount, select any frequency, click Calculate.
- **Observed:** `Number("Infinity")` evaluates to `Infinity`, which is not `NaN`, so the app
  accepts it and renders `Annual contribution: €∞` — a nonsensical, broken-looking result for
  what should be a rejected input.
- **Why this is ambiguous, not a clear-cut bug:** same reasoning as F1 — `Infinity` is,
  strictly speaking, a valid JavaScript `Number`, and R8 does not define magnitude limits.
- **Recommendation:** Same as F1 — a stricter decimal-only validation would also close this.
  Separately, and regardless of the "is this numeric" debate: rendering `€∞` to a user is a
  poor result on its own merits and is worth fixing even if the input-acceptance question
  stays open.
- **Automated evidence:** `CalculatorTest.testAnnualContributionCalculator[TC22_InfinityAmount_AmbiguousEdgeCase]` — fails as documented.

## Not a bug (ambiguity noted, no action needed)

- **Rounding mode:** R3/R5/R6 require specific decimal-place counts but do not state a rounding algorithm (HALF_UP vs HALF_EVEN vs DOWN). The automation's `CalculationUtils` documents this ambiguity and standardizes on `RoundingMode.HALF_UP` (standard commercial rounding) as its independent baseline. None of the 8 sample members land on an exact `*.x5` boundary at the relevant decimal place, so this ambiguity did not change any test outcome against the live app in this run. A dedicated utility-level test (`CalculationUtilsBoundaryTest`) now exercises the exact `0.00045` half-up tie and confirms the reference engine's own rounding is internally consistent with its documented HALF_UP baseline — this cannot be proven against the live app itself, since no fixture value sits at that exact boundary (see `EDGE_CASE_COVERAGE.md`).
- **Extremely large input overflow (`1e309` → `€∞`):** verified manually (not asserted automatically, since no business rule defines expected behavior at this magnitude) — JavaScript's floating-point arithmetic overflows to `Infinity` for absurdly large inputs. Not realistic for a contribution amount; documented as a low-likelihood robustness gap, not a confirmed defect.

---

## Daily Update (Slack, max 5 lines)

> Built and independently audited the Contribution Portal QA suite (Java/Selenium/TestNG, POM, Jackson, ExtentReports).
> Ran 54 tests: 38 passed, 16 failed, 0 framework errors — covering the calculator (22 cases), Members table (9 checks), and a new suite that proves case-insensitivity coverage the UI alone can't reach.
> Found 4 confirmed defects: fortnightly multiplier (24 vs 26), case-sensitive frequency lookup (now proven systemic across all 4 frequencies), missing negative-amount validation, and Order Status using unrounded units.
> Also documented 2 spec-ambiguity findings (hex/"Infinity" input) separately from the confirmed defects.
> Reports, bug log, traceability matrix, and docs are ready for submission.
