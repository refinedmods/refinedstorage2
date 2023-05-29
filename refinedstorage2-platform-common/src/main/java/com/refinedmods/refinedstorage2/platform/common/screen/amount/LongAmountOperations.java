package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import com.refinedmods.refinedstorage2.platform.common.util.MathHelper;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

public class LongAmountOperations implements AmountOperations<Long> {
    public static final AmountOperations<Long> INSTANCE = new LongAmountOperations();

    private LongAmountOperations() {
    }

    @Override
    public String format(final Long value) {
        return String.valueOf(value);
    }

    @Override
    public Optional<Long> parse(final String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> validate(final Long amount,
                                   @Nullable final Long minAmount,
                                   @Nullable final Long maxAmount) {
        final boolean minBoundOk = minAmount == null || amount >= minAmount;
        final boolean maxBoundOk = maxAmount == null || amount <= maxAmount;
        return minBoundOk && maxBoundOk ? Optional.of(amount) : Optional.empty();
    }

    @Override
    public Long changeAmount(final Long current,
                             final int delta,
                             @Nullable final Long minAmount,
                             @Nullable final Long maxAmount) {
        return MathHelper.clamp(
            current + delta,
            Objects.requireNonNullElse(minAmount, Long.MIN_VALUE),
            Objects.requireNonNullElse(maxAmount, Long.MAX_VALUE)
        );
    }
}
