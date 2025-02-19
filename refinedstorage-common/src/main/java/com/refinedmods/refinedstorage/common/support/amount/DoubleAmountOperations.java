package com.refinedmods.refinedstorage.common.support.amount;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class DoubleAmountOperations implements AmountOperations<Double> {
    public static final AmountOperations<Double> INSTANCE = new DoubleAmountOperations();

    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        final DecimalFormatSymbols initialAmountSymbols = new DecimalFormatSymbols(Locale.ROOT);
        initialAmountSymbols.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("##.###", initialAmountSymbols);
        DECIMAL_FORMAT.setGroupingUsed(false);
    }

    private DoubleAmountOperations() {
    }

    @Override
    public String format(final Double value) {
        return DECIMAL_FORMAT.format(value);
    }

    @Override
    public ParsedValue<Double> parse(final String value) {
        try {
            return new ParsedValue<>(Double.parseDouble(value), true);
        } catch (final NumberFormatException e) {
            return ParsedValue.invalid();
        }
    }

    @Override
    public Double changeAmount(@Nullable final Double current,
                               final int delta,
                               @Nullable final Double minAmount,
                               @Nullable final Double maxAmount) {
        final double correctedMinAmount = minAmount == null ? Double.MIN_VALUE : minAmount;
        final double correctedMaxAmount = maxAmount == null ? Double.MAX_VALUE : maxAmount;
        if (current == null) {
            return Mth.clamp(delta, correctedMinAmount, correctedMaxAmount);
        }
        return Mth.clamp(current + delta, correctedMinAmount, correctedMaxAmount);
    }
}
