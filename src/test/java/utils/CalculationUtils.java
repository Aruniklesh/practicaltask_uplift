package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * CalculationUtils serves as the Single Source of Truth for all financial
 * calculations and input validations based strictly on Business Rules R1 through R8 in README.md.
 *
 * NOTE ON FINANCIAL PRECISION & ROUNDING MODE AMBIGUITY:
 * ----------------------------------------------------
 * All financial arithmetic is performed using java.math.BigDecimal to avoid IEEE 754
 * binary floating-point rounding inaccuracies.
 *
 * SPECIFICATION AMBIGUITY FLAGGED:
 * Business Rule R3 states: "Money values show with a € sign and exactly 2 decimal places".
 * Business Rule R5 states: "Fund Value = Units Held × Unit Price, rounded to 2 decimal places".
 * However, the specification DOES NOT explicitly specify the rounding algorithm (e.g.
 * RoundingMode.HALF_UP vs RoundingMode.HALF_EVEN / Banker's Rounding vs RoundingMode.DOWN).
 * In this implementation, RoundingMode.HALF_UP (standard commercial/mathematical rounding)
 * is used as the default baseline. In a production banking environment, this ambiguity
 * must be formally clarified with the Product Owner.
 */
public class CalculationUtils {

    // Default rounding mode used when scaling financial values to 2 decimal places
    public static final RoundingMode DEFAULT_FINANCIAL_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Rule R1 & R2: Multipliers based on payment frequency.
     * Must be case-insensitive: weekly = 52, fortnightly = 26, monthly = 12, annual = 1.
     */
    public static int getMultiplier(String frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Payment frequency cannot be null");
        }
        switch (frequency.trim().toLowerCase()) {
            case "weekly":
                return 52;
            case "fortnightly":
                return 26; // Rule R1: 26 fortnights in a year (Application defect uses 24)
            case "monthly":
                return 12;
            case "annual":
                return 1;
            default:
                throw new IllegalArgumentException("Unsupported frequency: " + frequency);
        }
    }

    /**
     * Rule R1: Annual contribution = contribution amount × multiplier.
     * Uses BigDecimal for high-precision financial arithmetic.
     */
    public static BigDecimal calculateAnnualContribution(BigDecimal amount, String frequency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        BigDecimal multiplier = BigDecimal.valueOf(getMultiplier(frequency));
        return amount.multiply(multiplier).setScale(2, DEFAULT_FINANCIAL_ROUNDING);
    }

    public static BigDecimal calculateAnnualContribution(String amountStr, String frequency) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount string cannot be blank");
        }
        return calculateAnnualContribution(new BigDecimal(amountStr.trim()), frequency);
    }

    public static BigDecimal calculateAnnualContribution(double amount, String frequency) {
        return calculateAnnualContribution(BigDecimal.valueOf(amount), frequency);
    }

    /**
     * Rule R3: Formats a BigDecimal into European currency string: "€1,300.00".
     */
    public static String formatEuro(BigDecimal amount) {
        if (amount == null) return "€0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("€#,##0.00", symbols);
        return df.format(amount.setScale(2, DEFAULT_FINANCIAL_ROUNDING));
    }

    public static String formatEuro(double amount) {
        return formatEuro(BigDecimal.valueOf(amount));
    }

    /**
     * Rule R4: Units Held shows exactly 7 decimal places.
     */
    public static String formatUnitsHeld(BigDecimal units) {
        if (units == null) return "0.0000000";
        return units.setScale(7, RoundingMode.HALF_UP).toPlainString();
    }

    public static String formatUnitsHeld(double units) {
        return formatUnitsHeld(BigDecimal.valueOf(units));
    }

    /**
     * Rule R5: Fund Value = Units Held × Unit Price, rounded to 2 decimal places.
     */
    public static BigDecimal calculateFundValue(BigDecimal unitsHeld, BigDecimal unitPrice) {
        if (unitsHeld == null || unitPrice == null) {
            return BigDecimal.ZERO.setScale(2, DEFAULT_FINANCIAL_ROUNDING);
        }
        return unitsHeld.multiply(unitPrice).setScale(2, DEFAULT_FINANCIAL_ROUNDING);
    }

    public static BigDecimal calculateFundValue(double unitsHeld, double unitPrice) {
        return calculateFundValue(BigDecimal.valueOf(unitsHeld), BigDecimal.valueOf(unitPrice));
    }

    /**
     * Rule R6: Next Order Units = order quantity rounded to 4 decimal places.
     */
    public static BigDecimal roundOrderUnits(BigDecimal nextOrderUnits) {
        if (nextOrderUnits == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return nextOrderUnits.setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundOrderUnits(double nextOrderUnits) {
        return roundOrderUnits(BigDecimal.valueOf(nextOrderUnits));
    }

    public static String formatOrderUnits(BigDecimal nextOrderUnits) {
        return roundOrderUnits(nextOrderUnits).toPlainString();
    }

    public static String formatOrderUnits(double nextOrderUnits) {
        return formatOrderUnits(BigDecimal.valueOf(nextOrderUnits));
    }

    /**
     * Rule R7: Order Status must be Blocked if Next Order Units (after rounding to 4 decimal places)
     * is 0. Otherwise it is Ready.
     */
    public static String determineOrderStatus(BigDecimal nextOrderUnits) {
        BigDecimal roundedUnits = roundOrderUnits(nextOrderUnits);
        return (roundedUnits.compareTo(BigDecimal.ZERO) == 0) ? "Blocked" : "Ready";
    }

    public static String determineOrderStatus(double nextOrderUnits) {
        return determineOrderStatus(BigDecimal.valueOf(nextOrderUnits));
    }

    /**
     * Rule R8: Calculator Input Validation logic:
     * - Blank or non-numeric amount: "Enter a valid amount"
     * - Negative amount: "Amount cannot be negative"
     * - Zero is allowed (returns null, meaning no error)
     */
    public static String validateCalculatorInput(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return "Enter a valid amount";
        }
        try {
            BigDecimal value = new BigDecimal(rawInput.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                return "Amount cannot be negative";
            }
            return null; // Valid input (including zero and positive values)
        } catch (NumberFormatException e) {
            return "Enter a valid amount";
        }
    }
}
