package com.refinedmods.refinedstorage.common.support.amount;

import javax.annotation.Nullable;

import net.minecraft.util.Mth;

import static java.util.Objects.requireNonNullElse;

public class IntegerAmountOperations implements AmountOperations<Integer> {
    public static final AmountOperations<Integer> INSTANCE = new IntegerAmountOperations();

    private IntegerAmountOperations() {
    }

    @Override
    public String format(final Integer value) {
        return String.valueOf(value);
    }

    @Override
    public ReturnValue<Integer> parse(final String value) {
        try {
            return new ReturnValue<>(Integer.parseInt(value, 10), "");
        } catch (final NumberFormatException e) {
            return new ReturnValue<>("resource_amount_input.non_int");
        }
    }

    @Override
    public Integer changeAmount(@Nullable final Integer current,
                                final int delta,
                                @Nullable final Integer minAmount,
                                @Nullable final Integer maxAmount) {
        if (current == null) {
            return Mth.clamp(
                delta,
                requireNonNullElse(minAmount, Integer.MIN_VALUE),
                requireNonNullElse(maxAmount, Integer.MAX_VALUE)
            );
        }
        return Mth.clamp(
            current + delta,
            requireNonNullElse(minAmount, Integer.MIN_VALUE),
            requireNonNullElse(maxAmount, Integer.MAX_VALUE)
        );
    }
}
