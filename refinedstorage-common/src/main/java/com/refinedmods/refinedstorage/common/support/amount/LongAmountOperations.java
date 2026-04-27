package com.refinedmods.refinedstorage.common.support.amount;

import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class LongAmountOperations implements AmountOperations<Long> {
    public static final AmountOperations<Long> INSTANCE = new LongAmountOperations();

    private LongAmountOperations() {
    }

    @Override
    public String format(final Long value) {
        return String.valueOf(value);
    }

    @Override
    public ParsedValue<Long> parse(final String value) {
        try {
            return new ParsedValue<>(Long.parseLong(value));
        } catch (final NumberFormatException e) {
            return ParsedValue.invalid();
        }
    }

    @Override
    public Long changeAmount(@Nullable final Long current,
                             final int delta,
                             @Nullable final Long minAmount,
                             @Nullable final Long maxAmount) {
        final long correctedMinAmount = minAmount == null ? Long.MIN_VALUE : minAmount;
        final long correctedMaxAmount = maxAmount == null ? Long.MAX_VALUE : maxAmount;
        if (current == null) {
            return Mth.clamp(delta, correctedMinAmount, correctedMaxAmount);
        }
        return Mth.clamp(current + delta, correctedMinAmount, correctedMaxAmount);
    }
}
