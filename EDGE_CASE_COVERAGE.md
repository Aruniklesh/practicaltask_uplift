# Edge Case Coverage Matrix

Classification legend: **Covered-Pass** / **Covered-Fail** (a real, reproducible failure) /
**Partial** / **Not Covered** / **N/A**. All "Covered" entries were verified by actually
running the suite against `http://localhost:8000`, not assumed.

## A. Calculator edge cases

| Scenario | Classification | Test ID(s) | Notes |
|---|---|---|---|
| Empty input | Covered-Pass | `TC10` | |
| Whitespace-only input | Covered-Pass | `TC13` | |
| Zero | Covered-Pass | `TC07` | R8 explicitly allows zero |
| Negative integer | **Covered-Fail** | `TC12` | BUG-003 |
| Negative decimal | **Covered-Fail** | `TC14` | BUG-003 |
| Positive integer | Covered-Pass | `TC01`–`TC04` | |
| Positive decimal | Covered-Pass | `TC08` | |
| Very small decimal | Partial | — | No sub-cent amount (e.g. `0.001`) tested; low value given R3 only requires 2dp display and the app's arithmetic path is the same regardless of magnitude — recommended but not critical |
| Large amount | Covered-Pass | `TC09` | 10,000 — verifies comma grouping |
| Leading zeros | Covered-Pass | `TC18` | `"007"` → 7 |
| Trailing decimal point | Covered-Pass | `TC15` | `"5."` → 5, verified consistent in both JS and BigDecimal |
| Multiple decimal points | Covered-Pass | `TC20` | `"5..5"` correctly rejected by both JS and BigDecimal |
| Alphabetic characters | Covered-Pass | `TC11` | |
| Special characters | Covered-Pass | `TC19` | `"5,000"` (comma) correctly rejected |
| Currency symbols in input | Not Covered | — | `"€5"` was verified manually (Node) to be rejected consistently by both JS and Java, same code path as `TC11`/`TC19`; not added as a separate automated case since it would be a pure duplicate of existing coverage, not new risk |
| Extremely large numeric values | **Not Covered (documented risk)** | — | Verified manually: `1e309` overflows to `Infinity` in JS, rendering `€∞`. No business rule defines expected behavior at this magnitude, so no automated assertion was added (adding one would require fabricating a requirement). Documented as a low-likelihood robustness gap in `BUG_REPORTS.md`. |
| Repeated calculations | Not Covered | — | Clicking Calculate twice with the same inputs; app clears and re-renders `result`/`error` each click, so this is very low risk, but not automated |
| Changing amount after calculation | Not Covered | — | Same reasoning — the click handler fully re-reads both fields and clears previous output each time, so no state-leak risk was identified on inspection; recommended as a future regression test if the app ever introduces incremental/cached state |
| Changing frequency after calculation | Not Covered | — | Same as above |
| Submitting without changing values | N/A | — | No distinguishable behavior from the first calculation; nothing to assert |
| Hexadecimal notation (`"0x10"`) | **Covered-Fail (ambiguous)** | `TC21` | Finding F1 — see `BUG_REPORTS.md` |
| `"Infinity"` literal | **Covered-Fail (ambiguous)** | `TC22` | Finding F2 — see `BUG_REPORTS.md` |
| Scientific notation (`"1e3"`) | Covered-Pass | `TC17` | Confirmed consistent parsing between JS and BigDecimal |
| Leading plus sign (`"+25"`) | Covered-Pass | `TC16` | |

## B. Frequency edge cases

| Scenario | Classification | Test ID(s) | Notes |
|---|---|---|---|
| weekly | Covered-Pass | `TC01`, `TC05`, M001/M006 | |
| Weekly | **Covered-Pass (coincidental — see note)** | `ApplicationCoreLogicTest` | Lookup misses, falls back to `\|\| 52`, which equals weekly's own correct multiplier. Looks correct; does not prove case-insensitivity is implemented. |
| WEEKLY | Same as above | `ApplicationCoreLogicTest` | Same coincidental-pass caveat |
| fortnightly | **Covered-Fail** | `TC02`, `ApplicationCoreLogicTest`, M002/M008 | BUG-001 (24 instead of 26) |
| Fortnightly | **Covered-Fail (worse)** | `ApplicationCoreLogicTest` | Falls back to 52 — further from correct (26) than the lowercase form's defect (24) |
| FORTNIGHTLY | **Covered-Fail (worse)** | `ApplicationCoreLogicTest` | Same as above |
| monthly | Covered-Pass | `TC03`, `TC08`, M003/M007 | |
| Monthly | **Covered-Fail** | `ApplicationCoreLogicTest`, M004 | BUG-002 (confirmed defect, originally found via M004) |
| MONTHLY | **Covered-Fail** | `TC06` (dropdown-only, not genuine), `ApplicationCoreLogicTest` (genuine) | See Requirement Traceability Matrix note on `TC06` |
| annual | Covered-Pass | `TC04`, M005 | |
| Annual | **Covered-Fail** | `ApplicationCoreLogicTest` | Generalizes BUG-002 to this frequency — not previously proven by any member fixture |
| ANNUAL | **Covered-Fail** | `ApplicationCoreLogicTest` | Same |
| Leading/trailing whitespace in frequency | Not Covered | — | No member record or test exercises `" monthly "`; the app's lookup would miss (object keys are exact) and fall back to 52, same class of defect — not added as it would be a near-duplicate of the case-sensitivity finding, not new information |
| Unsupported frequency (e.g. `"biweekly"`) | Documented (not a confirmed defect) | `ApplicationCoreLogicTest.testUnsupportedFrequencyFallbackBehaviorIsDocumented` | No rule defines expected behavior; test documents the observed silent fallback to 52 as a potential risk, and guards against the behavior silently changing |

## C. Financial precision edge cases

| Scenario | Classification | Test ID(s) | Notes |
|---|---|---|---|
| Currency formatting (€, 2dp) | Covered-Pass | All SUCCESS calculator + member cases | |
| Units Held exactly 7dp | Covered-Pass | All 8 members | Verified no float drift at any fixture value |
| Next Order Units exactly 4dp | Covered-Pass | All 8 members, `CalculationUtilsBoundaryTest` | |
| Rounding half-up boundaries | **Partial** | `CalculationUtilsBoundaryTest` (`0.00045` exact tie) | Utility-level only; see Requirement Matrix Gap column |
| Values just below/above rounding boundaries | Covered-Pass (utility-level) | `CalculationUtilsBoundaryTest` (`0.000449`/`0.000451`, `0.99989`/`0.99996`) | |
| Trailing zeros | Covered-Pass | M003 (`unitsHeld: 2450.75` → `2450.7500000`) | |
| Zero fund value | Covered-Pass | M006 (`unitsHeld: 0`) | |
| Very small fund value | Covered-Pass | M008 (`1.0000005 × 9.9999 ≈ €10.00`) | |
| Large fund value | Covered-Pass | M002 (`980.1234567 × 2.1189 ≈ €2,076.78`) | |
| Floating-point precision risks | Covered-Pass (verified, not a defect) | `CalculationUtilsBoundaryTest.testFundValueRoundingAtHalfUpBoundary`; manual verification of the classic `1.005.toFixed(2)` trap | Confirmed the app's use of `toLocaleString` (not `toFixed`) for currency, and `CalculationUtils`' use of `BigDecimal.valueOf(double)` (not `new BigDecimal(double)`), both correctly avoid this well-known trap — a positive finding, not a gap |
| Negative/invalid source data in `members.json` | N/A | — | Not present in the client's fixture; cannot be added without modifying client-owned data |

## D. Order Status boundary cases (R6/R7)

| Rounds to | Classification | Test ID | Status |
|---|---|---|---|
| 0.0000 | Covered-Pass (utility) / **Covered-Fail (UI, M007)** | `CalculationUtilsBoundaryTest`, M006, M007 | M006 correctly Blocked; M007 incorrectly Ready (BUG-004) — raw value 0.00004 is non-zero so the app's buggy raw-value check says Ready even though the rounded value is 0.0000 |
| 0.0001 | Covered-Pass | `CalculationUtilsBoundaryTest`, M008 | |
| 0.0004 | Covered-Pass (utility only) | `CalculationUtilsBoundaryTest` | No fixture member lands exactly here |
| 0.0005 (exact half-up tie) | Covered-Pass (utility only) | `CalculationUtilsBoundaryTest` | Exact tie — rounding-mode ambiguity flagged, HALF_UP baseline documented |
| 0.9999 | Covered-Pass (utility only) | `CalculationUtilsBoundaryTest` | |
| 1.0000 | Covered-Pass (utility only) | `CalculationUtilsBoundaryTest` | Confirms a value that rounds up to exactly 1.0000 is still correctly non-zero/Ready in the reference engine |
| Raw non-zero that rounds to zero | **Covered-Fail (UI)** | M007 | This is precisely BUG-004 — the one boundary case the real fixture does contain, and it's the exact pattern that breaks |
| Both Blocked and Ready outcomes | Covered-Pass | M001–M008 (Ready: all except M006/M007\*; Blocked: M006) | \*M007 is Ready but *should* be Blocked — see BUG-004 |

**Honest conclusion on R6/R7 boundary coverage:** the single boundary case the real fixture
contains (M007) is exactly the case that exposes a real defect — strong evidence the existing
test design was already pointed in the right place. The additional boundary values requested
by this audit (0.0004, exact 0.0005 tie, 0.9999, 1.0000) are proven correct in the independent
reference engine, but **cannot** be proven against the live application without either new
fixture data (client-owned, must not be modified) or the client exposing the rounding logic as
a directly-callable function the way `annualFor` already is.
