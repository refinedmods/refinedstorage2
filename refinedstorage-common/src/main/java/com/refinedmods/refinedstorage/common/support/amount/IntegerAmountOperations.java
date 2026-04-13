package com.refinedmods.refinedstorage.common.support.amount;

import java.util.Optional;

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
    public Optional<Integer> parse(final String value) {
        try {
            return Optional.of(Integer.parseInt(value, 10));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> validate(final Integer amount,
                                      @Nullable final Integer minAmount,
                                      @Nullable final Integer maxAmount) {
        final boolean minBoundOk = minAmount == null || amount >= minAmount;
        final boolean maxBoundOk = maxAmount == null || amount <= maxAmount;
        return minBoundOk && maxBoundOk ? Optional.of(amount) : Optional.empty();
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
