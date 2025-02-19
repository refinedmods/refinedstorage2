package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.util.MathUtil;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNullElse;

public class LongAmountOperations implements AmountOperations<Long> {
    public static final AmountOperations<Long> INSTANCE = new LongAmountOperations();

    private LongAmountOperations() {
    }

    @Override
    public String format(@Nullable final Long value) {
        return (value == null) ? "" : String.valueOf(value);
    }

    @Override
    public ReturnValue<Long> parse(final String value) {
        try {
            return new ReturnValue<>(Long.parseLong(value), "");
        } catch (final NumberFormatException e) {
            return new ReturnValue<>("resource_amount_input.non_int");
        }
    }

    @Override
    public Long changeAmount(final Long current,
                             final int delta,
                             @Nullable final Long minAmount,
                             @Nullable final Long maxAmount) {
        return MathUtil.clamp(
            current + delta,
            requireNonNullElse(minAmount, Long.MIN_VALUE),
            requireNonNullElse(maxAmount, Long.MAX_VALUE)
        );
    }
}
