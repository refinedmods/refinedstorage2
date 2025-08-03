package com.refinedmods.refinedstorage.common.support.amount;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.util.Mth;

import static java.util.Objects.requireNonNullElse;

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
    public String format(@Nullable final Double value) {
        return (value == null) ? "" : DECIMAL_FORMAT.format(value);
    }

    @Override
    public ReturnValue<Double> parse(final String value) {
        try {
            return new ReturnValue<>(Double.parseDouble(value), "");
        } catch (final NumberFormatException e) {
            return new ReturnValue<>("resource_amount_input.non_int");
        }
    }

    @Override
    public Double changeAmount(@Nullable final Double current,
                               final int delta,
                               @Nullable final Double minAmount,
                               @Nullable final Double maxAmount) {
        if (current == null) {
            return Mth.clamp(
                delta,
                requireNonNullElse(minAmount, Double.MIN_VALUE),
                requireNonNullElse(maxAmount, Double.MAX_VALUE)
            );
        }
        return Mth.clamp(
            current + delta,
            requireNonNullElse(minAmount, Double.MIN_VALUE),
            requireNonNullElse(maxAmount, Double.MAX_VALUE)
        );
    }
}
