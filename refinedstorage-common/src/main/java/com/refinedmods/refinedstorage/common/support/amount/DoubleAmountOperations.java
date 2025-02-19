package com.refinedmods.refinedstorage.common.support.amount;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.util.Mth;

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
    public Double changeAmount(final Double current,
                               final int delta,
                               @Nullable final Double minAmount,
                               @Nullable final Double maxAmount) {
        return Mth.clamp(
            current + delta,
            Objects.requireNonNullElse(minAmount, Double.MIN_VALUE),
            Objects.requireNonNullElse(maxAmount, Double.MAX_VALUE)
        );
    }
}
