package com.refinedmods.refinedstorage2.platform.common.support.amount;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
    public String format(final Double value) {
        return DECIMAL_FORMAT.format(value);
    }

    @Override
    public Optional<Double> parse(final String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> validate(final Double amount,
                                     @Nullable final Double minAmount,
                                     @Nullable final Double maxAmount) {
        final boolean minBoundOk = minAmount == null || amount >= minAmount;
        final boolean maxBoundOk = maxAmount == null || amount <= maxAmount;
        return minBoundOk && maxBoundOk ? Optional.of(amount) : Optional.empty();
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
