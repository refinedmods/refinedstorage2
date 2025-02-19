package com.refinedmods.refinedstorage.common.support.amount;

import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class IntegerAmountOperations implements AmountOperations<Integer> {
    public static final AmountOperations<Integer> INSTANCE = new IntegerAmountOperations();

    private IntegerAmountOperations() {
    }

    @Override
    public String format(final Integer value) {
        return String.valueOf(value);
    }

    @Override
    public ParsedValue<Integer> parse(final String value) {
        try {
            return new ParsedValue<>(Integer.parseInt(value, 10), true);
        } catch (final NumberFormatException e) {
            return ParsedValue.invalid();
        }
    }

    @Override
    public Integer changeAmount(@Nullable final Integer current,
                                final int delta,
                                @Nullable final Integer minAmount,
                                @Nullable final Integer maxAmount) {
        final int correctedMinAmount = minAmount == null ? Integer.MIN_VALUE : minAmount;
        final int correctedMaxAmount = maxAmount == null ? Integer.MAX_VALUE : maxAmount;
        if (current == null) {
            return Mth.clamp(delta, correctedMinAmount, correctedMaxAmount);
        }
        return Mth.clamp(current + delta, correctedMinAmount, correctedMaxAmount);
    }
}
