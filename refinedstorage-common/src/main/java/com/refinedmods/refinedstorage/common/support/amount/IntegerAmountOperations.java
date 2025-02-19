package com.refinedmods.refinedstorage.common.support.amount;

import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.util.Mth;

public class IntegerAmountOperations implements AmountOperations<Integer> {
    public static final AmountOperations<Integer> INSTANCE = new IntegerAmountOperations();

    private IntegerAmountOperations() {
    }

    @Override
    public String format(@Nullable final Integer value) {
        return (value == null) ? "" : String.valueOf(value);
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
    public Integer changeAmount(final Integer current,
                                final int delta,
                                @Nullable final Integer minAmount,
                                @Nullable final Integer maxAmount) {
        return Mth.clamp(
            current + delta,
            Objects.requireNonNullElse(minAmount, Integer.MIN_VALUE),
            Objects.requireNonNullElse(maxAmount, Integer.MAX_VALUE)
        );
    }
}
